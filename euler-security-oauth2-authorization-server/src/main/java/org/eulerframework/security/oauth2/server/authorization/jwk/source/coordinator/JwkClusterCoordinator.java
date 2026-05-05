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
package org.eulerframework.security.oauth2.server.authorization.jwk.source.coordinator;

import org.eulerframework.security.oauth2.server.authorization.jwk.JwkRepository;
import org.eulerframework.security.oauth2.server.authorization.jwk.source.ClusteredReloadableJwkSource;
import org.eulerframework.security.oauth2.server.authorization.util.JwkUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * SPI that encapsulates the cross-node coordination backend used by
 * {@link ClusteredReloadableJwkSource}. The coordinator is
 * fingerprint-centric and intentionally avoids CAS: the authoritative
 * {@link JwkRepository} is the single source of truth, and any fingerprint
 * published here is a cache-invalidation hint that every node re-validates
 * by reloading from the repository.
 *
 * <h2>Capabilities</h2>
 * <ol>
 *   <li><b>Current fingerprint slot</b> &mdash; a single string value read by
 *       {@link #readCurrentFingerprint()} and written by
 *       {@link #setCurrentFingerprint(String, String)}. The backend is expected
 *       to implement a plain last-writer-wins overwrite; successive writes
 *       with the same value MUST still publish a hint so that a node that
 *       missed the previous message can still catch up.</li>
 *   <li><b>Hint channel</b> &mdash; {@link #hintFingerprintChanged(String, String)}
 *       broadcasts the current fingerprint without mutating state (used for a
 *       targeted refresh). Subscribers registered via
 *       {@link #addFingerprintHintListener(BiConsumer)} receive
 *       {@code (fingerprint, cause)} on every inbound hint.</li>
 *   <li><b>Node registry</b> &mdash; every node periodically reports its
 *       {@link NodeHeartbeat} so that operators can inspect the cluster via
 *       {@link #listNodes()} and {@code /admin/oauth2/jwks/cluster}.</li>
 * </ol>
 * Implementations own their lifecycle ({@link #start()} / {@link #stop()}): they
 * must not rely on {@link ClusteredReloadableJwkSource} to register message
 * listeners or open connections to the coordination backend.
 */
public interface JwkClusterCoordinator {

    /**
     * Read the latest persisted fingerprint. Implementations return
     * {@link JwkUtils#EMPTY_FINGERPRINT} when no fingerprint has been written
     * yet.
     *
     * @return cluster-wide SSOT fingerprint, never {@code null}
     */
    String readCurrentFingerprint();

    /**
     * Persist {@code fingerprint} as the cluster-wide SSOT and broadcast a hint
     * on the coordination channel. The write is a pure overwrite: the
     * coordinator does not inspect the previous value because the repository
     * is the authoritative data source and every receiver must reload and
     * re-compute the fingerprint locally anyway.
     *
     * @param fingerprint new SSOT fingerprint (must not be {@code null})
     * @param cause       free-form trigger tag forwarded to hint subscribers
     */
    void setCurrentFingerprint(String fingerprint, String cause);

    /**
     * Broadcast {@code fingerprint} as a hint without mutating the SSOT slot
     * (for example a targeted refresh directed at stragglers). Receivers
     * react by reloading and comparing against the repository; the hint is
     * purely advisory.
     *
     * @param fingerprint fingerprint that recipients should converge towards
     * @param cause       free-form trigger tag forwarded to hint subscribers
     */
    void hintFingerprintChanged(String fingerprint, String cause);

    /**
     * Subscribe {@code listener} to inbound fingerprint hints. Implementations
     * may accept multiple listeners and fan every inbound hint out to all of
     * them. Listener registration must survive a restart of the underlying
     * transport (re-registration during {@link #start()} is the expected
     * pattern).
     */
    void addFingerprintHintListener(BiConsumer<String, String> listener);

    /** Refresh this node's heartbeat record and membership in the cluster roster. */
    void heartbeat(String nodeId, NodeHeartbeat fields);

    /** Remove this node from the cluster roster and drop its heartbeat record. */
    void unregister(String nodeId);

    /** Snapshot every active node's heartbeat. */
    List<NodeStatus> listNodes();

    /**
     * Open connections to the coordination backend and wire up inbound hint
     * delivery. Invoked by {@link ClusteredReloadableJwkSource} during its own
     * {@link org.springframework.context.SmartLifecycle#start()} phase.
     */
    void start();

    /**
     * Release any resources owned by this coordinator (listener
     * registrations, scheduled tasks, etc.). Node-level cleanup (dropping the
     * heartbeat record) is driven by {@link #unregister(String)} and happens
     * before this method.
     */
    void stop();

    /**
     * Fields published on every heartbeat tick. {@code ttl} must exceed the
     * heartbeat interval configured in {@link ClusteredReloadableJwkSource.ClusteredReloadableJwkSourceOptions};
     * the coordinator is expected to apply the TTL to the backend-specific record.
     *
     * @param localFingerprint local aggregate {@code LiveState#fingerprint()} at
     *                         heartbeat time; {@link JwkUtils#EMPTY_FINGERPRINT}
     *                         when the node has not reloaded yet
     * @param heartbeatAt      wall-clock timestamp of this heartbeat tick
     * @param lastReloadAt     wall-clock timestamp of the node's last successful
     *                         reload, or {@code null} when the node has not
     *                         reloaded yet
     * @param host             originating host name
     * @param pid              originating process id
     * @param ttl              TTL applied to the heartbeat record
     */
    record NodeHeartbeat(String localFingerprint, Instant heartbeatAt, Instant lastReloadAt,
                         String host, String pid, Duration ttl) {
        public NodeHeartbeat {
            Objects.requireNonNull(localFingerprint, "localFingerprint");
            Objects.requireNonNull(heartbeatAt, "heartbeatAt");
            Objects.requireNonNull(ttl, "ttl");
            if (ttl.isNegative() || ttl.isZero()) {
                throw new IllegalArgumentException("ttl must be positive");
            }
        }
    }
}
