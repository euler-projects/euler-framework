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

package org.eulerframework.resource;

import org.springframework.util.Assert;

/**
 * Generic key-value tag.
 *
 * <p>{@code key} must be a non-null, non-blank string; {@code value} is allowed to be
 * {@code null}, indicating a key-only tag without a concrete value (e.g. a boolean-like
 * presence marker).</p>
 */
public record Tag(String key, String value) {

    public Tag {
        Assert.hasText(key, "Tag key must not be null or blank");
    }
}
