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
package org.eulerframework.security.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Keeps {@code UserIdentity} serializing through its getters, so the shape stays the flat envelope
 * plus {@code extensions}; widening field visibility instead would expose the backing
 * {@code properties} map the model deliberately hides.
 * <p>
 * {@code properties} is restated here because the model carries its own
 * {@link JsonIgnoreProperties}, and this mixin must not depend on how the two are merged.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonDeserialize(using = UserIdentityDeserializer.class)
@JsonIgnoreProperties(value = {"properties"}, ignoreUnknown = true)
abstract class UserIdentityMixin {

}
