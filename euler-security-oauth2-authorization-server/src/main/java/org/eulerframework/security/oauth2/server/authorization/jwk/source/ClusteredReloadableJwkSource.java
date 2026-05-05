/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.security.oauth2.server.authorization.jwk.source;

import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.proc.SecurityContext;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkEntry;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkRepository;
import org.eulerframework.security.oauth2.server.authorization.jwk.JwkRepositoryChangedEvent;
import org.eulerframework.security.oauth2.server.authorization.jwk.source.coordinator.JwkClusterCoordinator;
import org.eulerframework.security.oauth2.server.authorization.jwk.source.coordinator.NodeStatus;
import org.eulerframework.security.oauth2.server.authorization.util.JwkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Clustered {@link ReloadableJwkSource} that coordinates across nodes through a
 * {@link JwkClusterCoordinator}. The repository is the single source of truth;
 * the coordinator merely propagates <em>fingerprint hints</em> so peers know
 * to re-project the repository promptly.
 *
 * <h2>Convergence protocol</h2>
 * <ol>
 *   <li>On {@link #reload()} the node reads the repository, builds a
 *       {@link LiveState}, and skips if the aggregate fingerprint matches the
 *       current one (idempotent no-op).</li>
 *   <li>When the fingerprint changed the node commits the new {@link LiveState}
 *       locally, writes the fingerprint into the coordinator's SSOT slot (which
 *       also publishes a hint), refreshes its heartbeat.</li>
 *   <li>Incoming fingerprint hints trigger a {@link #reload()} unless the
 *       local fingerprint already matches the hint.</li>
 *   <li>A safety reload timer provides a fallback path in case a hint is
 *       missed (network hiccup, transient listener loss).</li>
 * </ol>
 * The heartbeat record carries {@code localFingerprint} + {@code lastReloadAt}
 * so operators can inspect convergence via
 * {@code /admin/oauth2/jwks/cluster} without touching the coordinator SSOT.
 */
public class ClusteredReloadableJwkSource implements ReloadableJwkSource, SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(ClusteredReloadableJwkSource.class);

    private final JwkRepository repository;
    private final JwkClusterCoordinator coordinator;
    private final ApplicationEventPublisher publisher;
    private final TaskScheduler scheduler;
    private final ClusteredReloadableJwkSourceOptions options;
    private final String nodeId;
    private final String host;
    private final String pid;
    private final ReentrantLock reloadLock = new ReentrantLock();

    private volatile LiveState state;
    private volatile Instant lastReloadAt;
    private volatile boolean running;
    private ScheduledFuture<?> heartbeatFuture;
    private ScheduledFuture<?> safetyReloadFuture;

    public ClusteredReloadableJwkSource(JwkRepository repository,
                                        JwkClusterCoordinator coordinator,
                                        ApplicationEventPublisher publisher,
                                        TaskScheduler scheduler,
                                        ClusteredReloadableJwkSourceOptions options) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.coordinator = Objects.requireNonNull(coordinator, "coordinator");
        this.publisher = Objects.requireNonNull(publisher, "publisher");
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.options = Objects.requireNonNull(options, "options");
        this.host = resolveHost();
        this.pid = resolvePid();
        this.nodeId = resolveNodeId(options.nodeId(), this.host, this.pid);
    }

    public String nodeId() {
        return this.nodeId;
    }

    public LiveState currentState() {
        return this.state;
    }

    /**
     * Snapshot of the coordinator SSOT fingerprint plus per-node heartbeat
     * summaries. Lagging nodes are those whose {@code localFingerprint} does
     * not match {@code currentFingerprint}.
     */
    public ClusterStatus clusterStatus() {
        String currentFp = readCoordinatorFingerprint();
        List<NodeStatus> nodes = this.coordinator.listNodes();
        return new ClusterStatus(currentFp, nodes);
    }

    /** Broadcast a targeted refresh hint to {@code nodeId} (advisory). */
    public void triggerNodeRefresh(String targetNodeId) {
        Objects.requireNonNull(targetNodeId, "targetNodeId");
        String fp = readCoordinatorFingerprint();
        this.coordinator.hintFingerprintChanged(fp, "refresh:" + targetNodeId);
    }

    /**
     * React to a {@link JwkRepositoryChangedEvent} on a best-effort basis.
     * Exceptions are logged and swallowed so a reload failure on this node
     * never propagates back to the management service that performed the
     * write.
     */
    @EventListener
    public void onJwkRepositoryChanged(JwkRepositoryChangedEvent event) {
        try {
            reload();
        }
        catch (Exception ex) {
            log.warn("Reload triggered by JwkRepositoryChangedEvent failed (nodeId={}, kid={}, cause={}): {}",
                    this.nodeId, event.getKid(), event.getCause(), ex.getMessage());
        }
    }

    // === ReloadableJwkSource ===

    @Override
    public void reload() {
        this.reloadLock.lock();
        try {
            List<JwkEntry> entries = this.repository.load();
            LiveState next = LiveState.build(entries);
            LiveState current = this.state;
            if (current != null && current.fingerprint().equals(next.fingerprint())) {
                log.debug("ClusteredReloadableJwkSource reload: fingerprint unchanged ({}), no-op",
                        next.fingerprint());
                return;
            }
            this.state = next;
            this.lastReloadAt = Instant.now();
            try {
                this.coordinator.setCurrentFingerprint(next.fingerprint(),
                        current == null ? "bootstrap" : "reload");
            }
            catch (Exception ex) {
                log.warn("Failed to publish fingerprint {} to coordinator: {}",
                        next.fingerprint(), ex.getMessage());
            }
            safeHeartbeat();
            log.info("ClusteredReloadableJwkSource reload: nodeId={} fingerprint={} signingKid={}",
                    this.nodeId, next.fingerprint(), next.signingKid());
        }
        finally {
            this.reloadLock.unlock();
        }
    }

    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) throws KeySourceException {
        LiveState snapshot = this.state;
        if (snapshot == null) {
            throw new KeySourceException("ClusteredReloadableJwkSource has not been initialised; "
                    + "call reload() before serving the first request");
        }
        return jwkSelector.select(snapshot.publishedSet());
    }

    // === SmartLifecycle ===

    @Override
    public synchronized void start() {
        if (this.running) {
            return;
        }
        this.coordinator.addFingerprintHintListener(this::onFingerprintHint);
        this.coordinator.start();
        // Bootstrap reload so get() works before the first hint arrives.
        reload();
        Instant first = Instant.now().plus(this.options.heartbeatInterval());
        this.heartbeatFuture = this.scheduler.scheduleAtFixedRate(this::safeHeartbeat,
                first, this.options.heartbeatInterval());
        Instant safetyFirst = Instant.now().plus(this.options.safetyReloadInterval());
        this.safetyReloadFuture = this.scheduler.scheduleAtFixedRate(this::safetyReload,
                safetyFirst, this.options.safetyReloadInterval());
        this.running = true;
        log.info("ClusteredReloadableJwkSource started (nodeId={}, heartbeat={}, safetyReload={})",
                this.nodeId, this.options.heartbeatInterval(), this.options.safetyReloadInterval());
    }

    @Override
    public synchronized void stop() {
        if (!this.running) {
            return;
        }
        this.running = false;
        cancel(this.heartbeatFuture);
        cancel(this.safetyReloadFuture);
        try {
            this.coordinator.unregister(this.nodeId);
        }
        catch (Exception ex) {
            log.warn("Failed to unregister node {}: {}", this.nodeId, ex.getMessage());
        }
        try {
            this.coordinator.stop();
        }
        catch (Exception ex) {
            log.warn("Failed to stop coordinator: {}", ex.getMessage());
        }
        log.info("ClusteredReloadableJwkSource stopped (nodeId={})", this.nodeId);
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    // === helpers ===

    private void onFingerprintHint(String fingerprint, String cause) {
        LiveState snapshot = this.state;
        if (snapshot != null && snapshot.fingerprint().equals(fingerprint)) {
            log.debug("Fingerprint hint {} matches local state; no reload needed (cause={})",
                    fingerprint, cause);
            return;
        }
        log.debug("Fingerprint hint {} differs from local {}; reloading (cause={})",
                fingerprint, snapshot == null ? "<null>" : snapshot.fingerprint(), cause);
        try {
            reload();
        }
        catch (Exception ex) {
            log.warn("Reload triggered by fingerprint hint {} failed: {}", fingerprint, ex.getMessage());
        }
    }

    private void safetyReload() {
        try {
            reload();
        }
        catch (Exception ex) {
            log.warn("Safety reload failed on node {}: {}", this.nodeId, ex.getMessage());
        }
    }

    private void safeHeartbeat() {
        LiveState snapshot = this.state;
        String fp = (snapshot == null) ? JwkUtils.EMPTY_FINGERPRINT : snapshot.fingerprint();
        JwkClusterCoordinator.NodeHeartbeat hb = new JwkClusterCoordinator.NodeHeartbeat(
                fp, Instant.now(), this.lastReloadAt, this.host, this.pid, this.options.heartbeatTtl());
        try {
            this.coordinator.heartbeat(this.nodeId, hb);
        }
        catch (Exception ex) {
            log.warn("Heartbeat failed for node {}: {}", this.nodeId, ex.getMessage());
        }
    }

    private String readCoordinatorFingerprint() {
        try {
            return this.coordinator.readCurrentFingerprint();
        }
        catch (Exception ex) {
            log.warn("Failed to read coordinator fingerprint: {}", ex.getMessage());
            LiveState snapshot = this.state;
            return (snapshot == null) ? JwkUtils.EMPTY_FINGERPRINT : snapshot.fingerprint();
        }
    }

    private static void cancel(ScheduledFuture<?> future) {
        if (future != null && !future.isCancelled()) {
            future.cancel(false);
        }
    }

    private static String resolveHost() {
        try {
            String name = java.net.InetAddress.getLocalHost().getHostName();
            return (name == null || name.isBlank()) ? "unknown" : name;
        }
        catch (Exception ex) {
            return "unknown";
        }
    }

    private static String resolvePid() {
        try {
            return Long.toString(ProcessHandle.current().pid());
        }
        catch (Exception ex) {
            try {
                String raw = ManagementFactory.getRuntimeMXBean().getName();
                int at = raw.indexOf('@');
                return (at > 0) ? raw.substring(0, at) : raw;
            }
            catch (Exception ignored) {
                return "0";
            }
        }
    }

    private static String resolveNodeId(String configured, String host, String pid) {
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        String rand = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        return host + "-" + pid + "-" + rand;
    }

    // Unused placeholder to keep Duration import referenced even if options change later.
    @SuppressWarnings("unused")
    private static Duration silencePlaceholder() {
        return Duration.ZERO;
    }

    /**
     * Backend-agnostic runtime options for {@link ClusteredReloadableJwkSource}.
     * Coordination-backend specifics (for example Redis key namespace) live on
     * the corresponding coordinator's own options record, e.g.
     * {@code RedisJwkClusterCoordinatorOptions}.
     *
     * @param nodeId               stable id of this node; blank means auto-generate as
     *                             {@code hostname-pid-rand6}
     * @param heartbeatInterval    period for refreshing the node's heartbeat record
     * @param heartbeatTtl         TTL applied to the heartbeat record; must exceed
     *                             {@code heartbeatInterval}
     * @param safetyReloadInterval fallback reload cadence in case a fingerprint hint is missed
     */
    public static record ClusteredReloadableJwkSourceOptions(
            String nodeId,
            Duration heartbeatInterval,
            Duration heartbeatTtl,
            Duration safetyReloadInterval) {

        public ClusteredReloadableJwkSourceOptions {
            Objects.requireNonNull(heartbeatInterval, "heartbeatInterval");
            Objects.requireNonNull(heartbeatTtl, "heartbeatTtl");
            Objects.requireNonNull(safetyReloadInterval, "safetyReloadInterval");
            if (heartbeatInterval.isNegative() || heartbeatInterval.isZero()) {
                throw new IllegalArgumentException("heartbeatInterval must be positive");
            }
            if (heartbeatTtl.compareTo(heartbeatInterval) <= 0) {
                throw new IllegalArgumentException(
                        "heartbeatTtl (" + heartbeatTtl + ") must exceed heartbeatInterval (" + heartbeatInterval + ")");
            }
            if (safetyReloadInterval.isNegative() || safetyReloadInterval.isZero()) {
                throw new IllegalArgumentException("safetyReloadInterval must be positive");
            }
        }
    }

    /**
     * Observability projection returned by {@code ClusteredReloadableJwkSource#clusterStatus()}.
     * {@code currentFingerprint} is the SSOT fingerprint recorded in the coordinator backend;
     * each {@link NodeStatus} reports its own last-heartbeat snapshot together with its
     * {@link NodeStatus#localFingerprint()} so operators can spot stragglers by simply
     * comparing fingerprints.
     *
     * @param currentFingerprint cluster-wide SSOT fingerprint at the coordinator
     * @param nodes              per-node heartbeat snapshots (sorted by nodeId)
     */
    public static record ClusterStatus(String currentFingerprint, List<NodeStatus> nodes) {

        public ClusterStatus {
            nodes = List.copyOf(nodes);
        }
    }
}
