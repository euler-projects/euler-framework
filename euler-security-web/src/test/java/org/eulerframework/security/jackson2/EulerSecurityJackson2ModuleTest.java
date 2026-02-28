/*
 * Copyright 2013-2024 the original author or authors.
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

package org.eulerframework.security.jackson2;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.jackson2.CoreJackson2Module;

import java.util.ArrayList;
import java.util.List;

public class EulerSecurityJackson2ModuleTest {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new CoreJackson2Module());
        mapper.registerModule(new EulerSecurityJackson2Module());
        List<Object> rawList = new ArrayList<>();
        rawList.add(new EulerUserDetails(EulerUserDetails.DEFAULT_TENANT_ID,"1", "1", "1", new ArrayList<>()));
        String json = mapper.writeValueAsString(rawList);
        System.out.println(json);
        List<Object> readList = mapper.readValue(json, List.class);
        System.out.println(mapper.writeValueAsString(readList));
    }
}