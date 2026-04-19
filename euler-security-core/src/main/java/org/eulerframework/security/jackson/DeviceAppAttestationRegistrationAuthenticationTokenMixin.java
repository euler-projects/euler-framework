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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eulerframework.security.authentication.appattest.DeviceAppAttestationRegistrationAuthenticationToken;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Jackson mixin for {@link DeviceAppAttestationRegistrationAuthenticationToken}.
 * <p>
 * Note: The registration endpoint currently does not persist this token to the
 * {@code SecurityContext}; it returns an HTTP JSON response directly. This mixin
 * is provided for forward-compatibility in case the token needs to be serialised
 * in the future (e.g. session persistence, event logging).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
		isGetterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(using = DeviceAppAttestationRegistrationAuthenticationTokenDeserializer.class)
abstract class DeviceAppAttestationRegistrationAuthenticationTokenMixin {

}
