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

package org.eulerframework.security.core.context;

import org.eulerframework.common.util.Assert;
import org.eulerframework.context.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

public class UserContextHolder {
    private static UserContext userContext;

    public static UserContext getUserContext() {
        if (userContext != null) {
            return userContext;
        }

        synchronized (UserContextHolder.class) {
            if (userContext != null) {
                return userContext;
            }

            return new DeferredUserContext(UserContextHolder::getUserContextBean);
        }
    }

    private static UserContext getUserContextBean() {
        ApplicationContext applicationContext = ApplicationContextHolder.getApplicationContext();
        Assert.notNull(applicationContext, "ApplicationContextHolder not initialized");
        userContext = applicationContext.getBean(UserContext.class);
        Assert.notNull(userContext, "No UserContext was defined");
        return userContext;
    }
}
