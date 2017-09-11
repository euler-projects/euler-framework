/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun (孙宾, SUN BIN) 
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
 * https://github.com/euler-form/web-form
 * https://cfrost.net
 */
package net.eulerframework.web.module.authentication.entity;

import java.util.Collection;
import java.util.UUID;

public class User implements EulerUserDetailsEntity {
    @Override
    public UUID getUserId() {
        return null;
    }

    @Override
    public Boolean isRoot() {
        return false;
    }

    @Override
    public Collection<EulerAuthorityEntity> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return "$2a$10$sS8B/EcIu5aSlw6Js9bosO3OG/Hn/LiJFwp13b.ep5Dlr3v0twJRy";
    }

    @Override
    public String getUsername() {
        return "admin";
    }

    @Override
    public Boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public Boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public Boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public Boolean isEnabled() {
        return false;
    }

    @Override
    public void eraseCredentials() {

    }
}
