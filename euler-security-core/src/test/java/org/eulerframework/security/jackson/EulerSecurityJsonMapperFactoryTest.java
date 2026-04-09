/*
 * Copyright 2013-2026 the original author or authors.
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

import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class EulerSecurityJsonMapperFactoryTest {

    @Test
    public void createJsonMapper() {
        JsonMapper jsonMapper = EulerSecurityJsonMapperFactory.getInstance();
        EulerUserDetails eulerUserDetails = EulerUserDetails.builder()
                .username("euler")
                .password("password")
                .authorities("user", "admin")
                .build();
        String json = jsonMapper.writeValueAsString(eulerUserDetails);
        EulerUserDetails deserializedObject = jsonMapper.readValue(json, EulerUserDetails.class);
        Assertions.assertEquals(eulerUserDetails.getUsername(), deserializedObject.getUsername());
        Assertions.assertEquals(eulerUserDetails.getPassword(), deserializedObject.getPassword());
        Assertions.assertEquals(eulerUserDetails.getAuthorities().size(), deserializedObject.getAuthorities().size());
        Assertions.assertEquals(eulerUserDetails.getAuthorities().iterator().next().getClass(), deserializedObject.getAuthorities().iterator().next().getClass());
    }

    @Test
    void durationTest() {
        Duration duration = Duration.ofMinutes(7);
        Map<String, Object> map = Collections.singletonMap("duration", duration);
        JsonMapper jsonMapper = EulerSecurityJsonMapperFactory.getInstance();
        String json = jsonMapper.writeValueAsString(map);
        System.out.println(json);
    }

}