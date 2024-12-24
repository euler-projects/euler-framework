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

package org.eulerframework.context;

import org.springframework.context.ApplicationContext;

public class ApplicationContextHolder {
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        if(ApplicationContextHolder.applicationContext != null) {
            throw new IllegalStateException("ApplicationContext is already initialized");
        }

        synchronized (ApplicationContextHolder.class) {
            if(ApplicationContextHolder.applicationContext != null) {
                throw new IllegalStateException("ApplicationContext is already initialized");
            }

            ApplicationContextHolder.applicationContext = applicationContext;
        }
    }
}
