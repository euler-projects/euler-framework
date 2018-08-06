/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 Euler Project 
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
 */
package net.eulerframework.web.core.cookie;

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
