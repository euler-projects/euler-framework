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
package org.eulerframework.web.module.authentication.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import org.eulerframework.web.module.authentication.util.UserDataValidator;

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
//        EulerUserEntity user = this.eulerUserEntityService.loadUserByUserId(userId);
        
//        UserDataValidator.validUsername(newUsername);
//        user.setUsername(newUsername.trim());
        
//        if(StringUtils.hasText(newEmail)) {
//            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
//                UserDataValidator.validEmail(newEmail);
//                user.setEmail(newEmail.trim());
//            }
//        } else {
//            user.setEmail(null);
//        }
//
//        if(StringUtils.hasText(newMobile)) {
//            if(!newMobile.equalsIgnoreCase(user.getMobile())) {
//                UserDataValidator.validMobile(newMobile);
//                user.setMobile(newMobile.trim());
//            }
//        } else {
//            user.setMobile(null);
//        }
//
//        this.eulerUserEntityService.updateUser(user);
        
        if(extraData != null && !extraData.isEmpty() && this.eulerUserExtraDataProcessors != null) {
            for(EulerUserExtraDataProcessor eulerUserExtraDataProcessor : this.eulerUserExtraDataProcessors) {
                if(eulerUserExtraDataProcessor.process(userId, extraData)) {
                    break;
                }
            }
        }
    }
}
