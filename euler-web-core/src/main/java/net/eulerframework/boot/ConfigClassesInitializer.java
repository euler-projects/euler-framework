/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://cfrost.net
 */
package net.eulerframework.boot;

import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.exception.EulerFrameworkInitException;

/**
 * @author cFrost
 *
 */
@Order(-1)
public class ConfigClassesInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected Class<?>[] getRootConfigClasses() {
        Class<?> rootConfigClass;
        try {
            rootConfigClass = Class.forName(WebConfig.getRootContextConfigClassName());
        } catch (ClassNotFoundException e1) {
            throw new EulerFrameworkInitException(e1.getMessage(), e1);
        }

        try {
            Class<?> securityConfigClass = Class
                    .forName("net.eulerframework.web.module.authentication.conf.SecurityConfiguration");
            return new Class[] { securityConfigClass, rootConfigClass };
        } catch (ClassNotFoundException e) {
            return new Class[] { rootConfigClass };
        }
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String[] getServletMappings() {
        // TODO Auto-generated method stub
        return null;
    }
}
