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
package org.eulerframework.web.core.i18n;

import jakarta.servlet.http.HttpServletRequest;

import org.eulerframework.context.ApplicationContextHolder;

import org.eulerframework.web.servlet.util.ServletUtils;

public class Tag {

    public static String i18n(String msgKey) {
        if(msgKey == null) {
            return null;
        }

        HttpServletRequest request = ServletUtils.getRequest();
        return ApplicationContextHolder.getApplicationContext().getMessage(
                msgKey,
                null,
                msgKey,
                request.getLocale());
    }
}
