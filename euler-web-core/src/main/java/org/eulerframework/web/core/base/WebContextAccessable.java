/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.web.core.base;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.eulerframework.common.base.log.LogSupport;


public abstract class WebContextAccessable extends LogSupport {
    
    private ServletRequestAttributes getServletRequestAttributes() {
        return (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    }

    protected ServletContext getServletContext(){
        return this.getRequest().getServletContext();
    }
    
    protected HttpServletRequest getRequest() {
        return this.getServletRequestAttributes().getRequest();
    }
    
    protected HttpServletResponse getResponse() {
        return this.getServletRequestAttributes().getResponse();
    }

}
