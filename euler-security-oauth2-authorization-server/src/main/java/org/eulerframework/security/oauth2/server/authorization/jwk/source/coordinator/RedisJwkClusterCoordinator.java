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

import org.eulerframework.security.oauth2.server.authorization.jwk.source.ClusteredReloadableJwkSource;
import org.eulerframework.security.oauth2.server.authorization.util.JwkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/**
 * Redis-backed {@link JwkClusterCoordinator}. The cluster-wide SSOT fingerprint
 * lives at {@code {ns}:current-fingerprint} as a plain string; fingerprint-change
 * hints travel over the {@code {ns}:fp-change} pub/sub channel; heartbeat records
 * live at {@code {ns}:nodes:{nodeId}} hashes with membership tracked in the
 * {@code {ns}:nodes} set.
 * <p>
 * There is no Lua CAS: the repository is the authoritative source of truth,
 * every write is an idempotent overwrite, and every receiver reloads and
 * recomputes the fingerprint locally before deciding whether the hint matters.
 * Message format on the pub/sub channel is {@code "fingerprint|cause"} (cause
 * may be empty).
 */
public class RedisJwkClusterCoordinator implements JwkClusterCoordinator, MessageListener {

    private static final Logger log = LoggerFactory.getLogger(RedisJwkClusterCoordinator.class);

    private final StringRedisTemplate redis;
    private final RedisMessageListenerContainer listeners;
    private final RedisJwkClusterCoordinatorOptions options;

    private final String fingerprintKey;
    private final ChannelTopic hintTopic;
    private final String nodesSetKey;

    private final List<BiConsumer<String, String>> subscribers = new CopyOnWriteArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public RedisJwkClusterCoordinator(StringRedisTemplate redis,
                                      RedisMessageListenerContainer listeners,
                                      RedisJwkClusterCoordinatorOptions options) {
        this.redis = Objects.requireNonNull(redis, "redis");
        this.listeners = Objects.requireNonNull(listeners, "listeners");
        this.options = Objects.requireNonNull(options, "options");

        String ns = options.namespace();
        this.fingerprintKey = ns + ":current-fingerprint";
        this.hintTopic = new ChannelTopic(ns + ":fp-change");
        this.nodesSetKey = ns + ":nodes";
    }

    // === JwkClusterCoordinator ===

    @Override
    public String readCurrentFingerprint() {
        String value = this.redis.opsForValue().get(this.fingerprintKey);
        return (value == null || value.isEmpty()) ? JwkUtils.EMPTY_FINGERPRINT : value;
    }

    @Override
    public void setCurrentFingerprint(String fingerprint, String cause) {
        Objects.requireNonNull(fingerprint, "fingerprint");
        this.redis.opsForValue().set(this.fingerprintKey, fingerprint);
        publishHint(fingerprint, cause);
    }

    @Override
    public void hintFingerprintChanged(String fingerprint, String cause) {
        Objects.requireNonNull(fingerprint, "fingerprint");
        publishHint(fingerprint, cause);
    }

    private void publishHint(String fingerprint, String cause) {
        String payload = fingerprint + "|" + (cause == null ? "" : cause);
        this.redis.convertAndSend(this.hintTopic.getTopic(), payload);
    }

    @Override
    public void addFingerprintHintListener(BiConsumer<String, String> listener) {
        Objects.requireNonNull(listener, "listener");
        this.subscribers.add(listener);
    }

    @Override
    public void heartbeat(String nodeId, NodeHeartbeat fields) {
        Objects.requireNonNull(nodeId, "nodeId");
        Objects.requireNonNull(fields, "fields");
        String key = nodeKey(nodeId);
        Map<String, String> hash = new HashMap<>();
        hash.put("localFingerprint", fields.localFingerprint());
        hash.put("heartbeatAt", fields.heartbeatAt().toString());
        hash.put("lastReloadAt", fields.lastReloadAt() == null ? "" : fields.lastReloadAt().toString());
        hash.put("host", (fields.host() == null) ? "unknown" : fields.host());
        hash.put("pid", (fields.pid() == null) ? "0" : fields.pid());
        this.redis.opsForHash().putAll(key, new HashMap<>(hash));
        this.redis.expire(key, fields.ttl());
        this.redis.opsForSet().add(this.nodesSetKey, nodeId);
    }

    @Override
    public void unregister(String nodeId) {
        Objects.requireNonNull(nodeId, "nodeId");
        try {
            this.redis.opsForSet().remove(this.nodesSetKey, nodeId);
            this.redis.delete(nodeKey(nodeId));
        }
        catch (Exception ex) {
            log.warn("Failed to clean up node registration (nodeId={}): {}", nodeId, ex.getMessage());
        }
    }

    @Override
    public List<NodeStatus> listNodes() {
        Set<String> active = this.redis.opsForSet().members(this.nodesSetKey);
        if (active == null || active.isEmpty()) {
            return List.of();
        }
        List<NodeStatus> statuses = new ArrayList<>(active.size());
        for (String id : active) {
            statuses.add(readNodeStatus(id));
        }
        statuses.sort(Comparator.comparing(NodeStatus::nodeId));
        return statuses;
    }

    @Override
    public void start() {
        if (!this.running.compareAndSet(false, true)) {
            return;
        }
        this.listeners.addMessageListener(this, this.hintTopic);
        log.info("RedisJwkClusterCoordinator started (namespace={})", this.options.namespace());
    }

    @Override
    public void stop() {
        if (!this.running.compareAndSet(true, false)) {
            return;
        }
        try {
            this.listeners.removeMessageListener(this, this.hintTopic);
        }
        catch (Exception ex) {
            log.warn("Failed to remove Redis message listener on stop: {}", ex.getMessage());
        }
        log.info("RedisJwkClusterCoordinator stopped (namespace={})", this.options.namespace());
    }

    // === MessageListener ===

    @Override
    public void onMessage(Message message, byte[] pattern) {
        byte[] body = message.getBody();
        if (body == null || body.length == 0) {
            return;
        }
        String payload = new String(body, StandardCharsets.UTF_8);
        int sep = payload.indexOf('|');
        String fingerprint = (sep < 0) ? payload : payload.substring(0, sep);
        String cause = (sep < 0) ? "" : payload.substring(sep + 1);
        if (fingerprint.isEmpty()) {
            return;
        }
        for (BiConsumer<String, String> subscriber : this.subscribers) {
            try {
                subscriber.accept(fingerprint, cause);
            }
            catch (Exception ex) {
                log.warn("JwkClusterCoordinator listener failed for hint fp={} cause={}: {}",
                        fingerprint, cause, ex.getMessage());
            }
        }
    }

    // === helpers ===

    private NodeStatus readNodeStatus(String id) {
        Map<Object, Object> raw = this.redis.opsForHash().entries(nodeKey(id));
        if (raw == null || raw.isEmpty()) {
            return NodeStatus.missing(id);
        }
        String localFingerprint = asString(raw.get("localFingerprint"));
        if (localFingerprint != null && localFingerprint.isEmpty()) {
            localFingerprint = null;
        }
        Instant heartbeatAt = parseInstantSafely(asString(raw.get("heartbeatAt")));
        Instant lastReloadAt = parseInstantSafely(asString(raw.get("lastReloadAt")));
        return new NodeStatus(id, false, localFingerprint, heartbeatAt, lastReloadAt,
                asString(raw.get("host")), asString(raw.get("pid")));
    }

    private String nodeKey(String id) {
        return this.options.namespace() + ":nodes:" + id;
    }

    private static String asString(Object value) {
        return (value == null) ? null : value.toString();
    }

    private static Instant parseInstantSafely(String iso) {
        if (iso == null || iso.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(iso);
        }
        catch (Exception ex) {
            return null;
        }
    }

    // Exposed for tests to assert key layout.
    String fingerprintKey() {
        return this.fingerprintKey;
    }

    String nodesSetKey() {
        return this.nodesSetKey;
    }

    ChannelTopic hintTopic() {
        return this.hintTopic;
    }

    /**
     * Redis-specific options for {@link RedisJwkClusterCoordinator}. Keeps the
     * Redis key prefix out of the backend-agnostic
     * {@link ClusteredReloadableJwkSource.ClusteredReloadableJwkSourceOptions}, so that alternative coordinators
     * (etcd, zk, ...) can introduce their own sibling options record without
     * polluting the shared surface.
     *
     * @param namespace Redis key prefix shared by every cluster member
     */
    public static record RedisJwkClusterCoordinatorOptions(String namespace) {

        public RedisJwkClusterCoordinatorOptions {
            Objects.requireNonNull(namespace, "namespace");
            if (namespace.isBlank()) {
                throw new IllegalArgumentException("namespace must not be blank");
            }
        }
    }
}
