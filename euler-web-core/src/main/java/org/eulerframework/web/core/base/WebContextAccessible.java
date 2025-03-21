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
package org.eulerframework.web.core.base;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eulerframework.web.util.ServletUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class WebContextAccessible {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected ServletContext getServletContext() {
        return ServletUtils.getServletContext();
    }

    protected HttpServletRequest getRequest() {
        return ServletUtils.getRequest();
    }

    protected HttpServletResponse getResponse() {
        return ServletUtils.getResponse();
    }

}
