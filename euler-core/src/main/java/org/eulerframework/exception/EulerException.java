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

package org.eulerframework.exception;

import org.eulerframework.context.ApplicationContextHolder;
import org.springframework.context.ApplicationContext;

import java.util.Locale;

public class EulerException extends Exception {

    public EulerException() {
        super();
    }

    public EulerException(String message) {
        super(message);
    }

    public EulerException(Throwable cause) {
        super(cause);
    }

    public EulerException(String message, Throwable cause) {
        super(message, cause);
    }

    protected EulerException(String message, Throwable cause, boolean enableSuppression,
                             boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    @Override
    public String getLocalizedMessage() {
        ApplicationContext applicationContext = ApplicationContextHolder.getApplicationContext();

        if(applicationContext == null) {
            return super.getLocalizedMessage();
        }

        return applicationContext.getMessage(this.getMessage(), null, Locale.SIMPLIFIED_CHINESE);
    }
}
