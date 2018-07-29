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
package net.eulerframework.web.module.authentication.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import net.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * @author cFrost
 *
 */
@Service
public class EulerUserExtraDataService {

    @Autowired
    private EulerUserEntityService eulerUserEntityService;
    @Autowired(required = false) 
    private List<EulerUserExtraDataProcessor> eulerUserExtraDataProcessors;

    public void updateUserWithExtraData(
            String userId, 
            //String newUsername, 
            String newEmail, 
            String newMobile,
            Map<String, Object> extraData) 
            throws UserInfoCheckWebException {
        EulerUserEntity user = this.eulerUserEntityService.loadUserByUserId(userId);
        
//        UserDataValidator.validUsername(newUsername);
//        user.setUsername(newUsername.trim());
        
        if(StringUtils.hasText(newEmail)) {
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                UserDataValidator.validEmail(newEmail);
                user.setEmail(newEmail.trim());
            }
        } else {
            user.setEmail(null);
        }
        
        if(StringUtils.hasText(newMobile)) { 
            if(!newMobile.equalsIgnoreCase(user.getMobile())) {
                UserDataValidator.validMobile(newMobile);
                user.setMobile(newMobile.trim());
            }
        } else {
            user.setMobile(null);
        }
        
        this.eulerUserEntityService.updateUser(user);
        
        if(extraData != null && !extraData.isEmpty() && this.eulerUserExtraDataProcessors != null) {
            for(EulerUserExtraDataProcessor eulerUserExtraDataProcessor : this.eulerUserExtraDataProcessors) {
                if(eulerUserExtraDataProcessor.process(userId, extraData)) {
                    break;
                }
            }
        }
    }
}
