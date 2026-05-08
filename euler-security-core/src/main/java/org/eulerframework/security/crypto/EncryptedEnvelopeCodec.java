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
package org.eulerframework.security.crypto;

import jakarta.annotation.Nullable;
import org.springframework.util.Assert;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * Pure codec that assembles and disassembles the on-disk data-encryption
 * envelope string.
 *
 * <p>Wire format (variable-length colon-separated segments):
 *
 * <pre>
 *   &lt;b64url(header_json)&gt;:&lt;b64url(bodyPart1)&gt;[:&lt;b64url(bodyPart2)&gt;[…]]
 * </pre>
 *
 * <p>Total segment count is {@code 1(header) + N(body)} where {@code N} is
 * decided by the algorithm (AEAD: {@code [iv, ctWithTag]} → 3 total;
 * plaintext: {@code [plaintext]} → 2 total). Body segment semantics are
 * opaque to this codec.
 *
 * <p>The {@code header_json} is a minimised JSON object:
 *
 * <pre>{"v":1,"alg":"&lt;algorithmId&gt;","kid":"&lt;kid&gt;"}</pre>
 *
 * <p>The {@code kid} field is optional — omitted entirely for algorithms
 * without a KEY (e.g. {@code noop}). Unknown extra header fields are
 * tolerated on decode to keep the format forward-compatible. Segment
 * separator {@code ':'} is not part of the Base64URL alphabet.
 */
public final class EncryptedEnvelopeCodec {

    /** Current envelope version. */
    public static final int VERSION = 1;

    private static final String SEPARATOR = ":";
    private static final Base64.Encoder B64URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64URL_DECODER = Base64.getUrlDecoder();

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private EncryptedEnvelopeCodec() {
    }

    /**
     * Assemble an envelope string. Each of {@code bodyParts} is Base64URL-
     * encoded as-is; the header is built from the supplied {@code algorithmId}
     * and optional {@code kid}. {@code kid} may be {@code null} for
     * algorithms without a KEY; in that case the {@code kid} field is omitted
     * from the header JSON.
     */
    public static String encode(String algorithmId, @Nullable String kid, byte[]... bodyParts) {
        Assert.hasText(algorithmId, "algorithmId is required");
        Objects.requireNonNull(bodyParts, "bodyParts");
        if (bodyParts.length < 1) {
            throw new IllegalArgumentException("At least one body part is required");
        }
        ObjectNode header = MAPPER.createObjectNode();
        header.put("v", VERSION);
        header.put("alg", algorithmId);
        if (kid != null) {
            header.put("kid", kid);
        }
        byte[] headerBytes;
        try {
            headerBytes = MAPPER.writeValueAsBytes(header);
        }
        catch (JacksonException ex) {
            // Building a fixed-shape ObjectNode from literals cannot fail in practice.
            throw new IllegalStateException("Failed to serialize envelope header", ex);
        }
        StringBuilder sb = new StringBuilder();
        sb.append(B64URL_ENCODER.encodeToString(headerBytes));
        for (byte[] part : bodyParts) {
            Objects.requireNonNull(part, "body part");
            sb.append(SEPARATOR).append(B64URL_ENCODER.encodeToString(part));
        }
        return sb.toString();
    }

    /**
     * Parse a previously {@link #encode encoded} envelope string. Throws
     * {@link IllegalArgumentException} for any structural defect (too few
     * segments, invalid Base64URL, malformed header JSON, missing required
     * header fields, unsupported version).
     */
    public static Envelope decode(String envelope) {
        Assert.hasText(envelope, "envelope is required");
        String[] segments = envelope.split(SEPARATOR, -1);
        if (segments.length < 2) {
            throw new IllegalArgumentException("Malformed envelope: expected at least 2 segments, got " + segments.length);
        }
        byte[] headerBytes;
        List<byte[]> bodyParts = new ArrayList<>(segments.length - 1);
        try {
            headerBytes = B64URL_DECODER.decode(segments[0]);
            for (int i = 1; i < segments.length; i++) {
                bodyParts.add(B64URL_DECODER.decode(segments[i]));
            }
        }
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Malformed envelope: invalid Base64URL content", ex);
        }
        JsonNode header;
        try {
            header = MAPPER.readTree(headerBytes);
        }
        catch (JacksonException ex) {
            throw new IllegalArgumentException("Malformed envelope: header is not valid JSON", ex);
        }
        if (!header.isObject()) {
            throw new IllegalArgumentException("Malformed envelope: header is not a JSON object");
        }
        JsonNode versionNode = header.get("v");
        if (versionNode == null || !versionNode.canConvertToInt()) {
            throw new IllegalArgumentException("Malformed envelope: header missing 'v'");
        }
        int version = versionNode.asInt();
        if (version != VERSION) {
            throw new IllegalArgumentException("Unsupported envelope version: " + version);
        }
        String alg = requiredTextField(header, "alg");
        String kid = optionalTextField(header, "kid");
        return new Envelope(version, alg, kid, List.copyOf(bodyParts));
    }

    private static String requiredTextField(JsonNode header, String name) {
        JsonNode node = header.get(name);
        if (node == null || !node.isTextual() || node.asString().isEmpty()) {
            throw new IllegalArgumentException("Malformed envelope: header missing '" + name + "'");
        }
        return node.asString();
    }

    @Nullable
    private static String optionalTextField(JsonNode header, String name) {
        JsonNode node = header.get(name);
        if (node == null || node.isNull()) {
            return null;
        }
        if (!node.isTextual() || node.asString().isEmpty()) {
            throw new IllegalArgumentException("Malformed envelope: header field '" + name + "' must be a non-empty string when present");
        }
        return node.asString();
    }

    /**
     * Decoded envelope. {@code bodyParts} holds the raw bytes of each body
     * segment in the order they were produced by the encrypting algorithm.
     * {@code kid} is {@code null} for algorithms that omit the field.
     */
    public record Envelope(int version, String algorithmId, @Nullable String kid, List<byte[]> bodyParts) {
    }

    /** Convenience for callers that already have UTF-8 plaintext. */
    public static byte[] toUtf8(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    /** Convenience for callers that want UTF-8 out. */
    public static String fromUtf8(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
