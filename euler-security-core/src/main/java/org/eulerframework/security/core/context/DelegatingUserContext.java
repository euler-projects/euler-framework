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
package org.eulerframework.security.core.context;

import org.eulerframework.security.core.userdetails.EulerUserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DelegatingUserContext implements UserContext {
    private final List<UserContext> userContexts = new ArrayList<>();

    public DelegatingUserContext(UserContext... userContext) {
        this.userContexts.addAll(Arrays.asList(userContext));
    }

    @Override
    public EulerUserDetails getUserDetails() {
        EulerUserDetails userDetails = null;
        for (UserContext userContext : this.userContexts) {
            if ((userDetails = userContext.getUserDetails()) != null) {
                break;
            }
        }
        return userDetails;
    }

    @Override
    public String getUserId() {
        String userId = null;
        for (UserContext userContext : this.userContexts) {
            if ((userId = userContext.getUserId()) != null) {
                break;
            }
        }
        return userId;
    }

    @Override
    public String getTenantId() {
        String tenantId = null;
        for (UserContext userContext : this.userContexts) {
            if ((tenantId = userContext.getTenantId()) != null) {
                break;
            }
        }
        return tenantId;
    }
}
