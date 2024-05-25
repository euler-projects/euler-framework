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
package org.eulerframework.web.core.cookie;

/**
 * @author cFrost
 *
 */
public enum LocaleCookies implements EulerCookies {
    LOCALE("EULER_LOCALE", 10 * 365 * 24 * 60 * 60, "/");
    
    LocaleCookies(String name, int age, String path) {
        this.age = age;
        this.name = name;
        this.path = path;
    }
    
    private String name;
    private int age;
    private String path;

    @Override
    public String getCookieName() {
        // TODO Auto-generated method stub
        return name;
    }

    @Override
    public int getCookieAge() {
        // TODO Auto-generated method stub
        return age;
    }

    @Override
    public String getCookiePath() {
        // TODO Auto-generated method stub
        return path;
    }

}
