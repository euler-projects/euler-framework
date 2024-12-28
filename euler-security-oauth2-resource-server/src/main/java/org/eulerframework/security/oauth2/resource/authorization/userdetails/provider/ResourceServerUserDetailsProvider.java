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

package org.eulerframework.security.oauth2.resource.authorization.userdetails.provider;

import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.eulerframework.security.oauth2.core.userdetails.provider.OAuth2TokenUserDetailsProvider;

public class ResourceServerUserDetailsProvider implements OAuth2TokenUserDetailsProvider {

    @Override
    public EulerUserDetails provide(String principal) {
        return EulerUserDetails.builder()
                .userId(principal)
                .username(principal)
                .password("")
                .credentialsExpired(true)
                .build();
    }
}
