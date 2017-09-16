/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
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
package net.eulerframework.web.module.authentication.service;

import javax.annotation.Resource;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import net.eulerframework.web.module.authentication.exception.InvalidEmailResetTokenException;
import net.eulerframework.web.module.authentication.exception.InvalidSMSResetCodeException;

/**
 * @author cFrost
 *
 */
@Service("passwordService")
public class PasswordServiceImpl implements PasswordService {
    
    @Resource private PasswordEncoder passwordEncoder;
    @Resource private EulerUserEntityService eulerUserEntityService;

    @Override
    public PasswordEncoder getPasswordEncoder() {
        return this.passwordEncoder;
    }

    @Override
    public EulerUserEntityService getEulerUserEntityService() {
        return this.eulerUserEntityService;
    }

    @Override
    public void passwdResetSMSGen(String mobile) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void passwdResetEmailGen(String email) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String analyzeUserIdFromSMSResetPin(String pin) throws InvalidSMSResetCodeException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String analyzeUserIdFromEmailResetToken(String token) throws InvalidEmailResetTokenException {
        // TODO Auto-generated method stub
        return null;
    }

}
