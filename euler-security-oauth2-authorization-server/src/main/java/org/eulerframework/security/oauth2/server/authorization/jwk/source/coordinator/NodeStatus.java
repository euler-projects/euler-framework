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

import java.time.Instant;

/**
 * Per-node heartbeat snapshot recorded in {@code {ns}:nodes:{nodeId}}.
 * {@code missing=true} indicates the node is registered but its heartbeat hash
 * has expired (TTL lapsed). Convergence is inspected by comparing
 * {@link #localFingerprint()} against {@link ClusteredReloadableJwkSource.ClusterStatus#currentFingerprint()}:
 * equal means in-sync, different means the node has not yet observed the
 * latest cluster state.
 *
 * @param nodeId           stable id as registered in {@code {ns}:nodes}
 * @param missing          {@code true} when the heartbeat hash is absent (TTL expired)
 * @param localFingerprint the node's last-committed {@code LiveState#fingerprint()};
 *                         {@code null} when {@code missing}
 * @param heartbeatAt      timestamp of the last heartbeat; {@code null} when {@code missing}
 * @param lastReloadAt     timestamp of the node's last successful reload;
 *                         {@code null} when the node has not yet reloaded
 * @param host             originating host name at startup
 * @param pid              originating process id at startup
 */
public record NodeStatus(
        String nodeId,
        boolean missing,
        String localFingerprint,
        Instant heartbeatAt,
        Instant lastReloadAt,
        String host,
        String pid) {

    /** Build a placeholder {@code NodeStatus} for a node whose heartbeat hash has expired. */
    public static NodeStatus missing(String nodeId) {
        return new NodeStatus(nodeId, true, null, null, null, null, null);
    }
}
