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
package org.eulerframework.web.module.authentication.controller.ajax.settings.profile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.eulerframework.web.core.annotation.AjaxController;
import org.eulerframework.web.core.annotation.ApiEndpoint;
import org.eulerframework.web.core.base.controller.ApiSupportWebController;
import org.eulerframework.web.module.authentication.context.UserContext;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.service.EulerUserExtraDataProcessor;
import org.eulerframework.web.module.authentication.service.EulerUserExtraDataService;

/**
 * @author cFrost
 *
 */
@AjaxController
@ApiEndpoint
@RequestMapping("settings/profile")
public class ProfileSettingsAjaxController extends ApiSupportWebController {

    @Autowired
    private EulerUserExtraDataService eulerUserExtraDataService;
    @Autowired(required = false) 
    private List<EulerUserExtraDataProcessor> eulerUserExtraDataProcessors;
    
    @GetMapping
    public Map<String, Object> findUserProfile() {
        EulerUserEntity userEntity = UserContext.getCurrentUserEntity();
        
        Map<String, Object> userData = new HashMap<>();

        userData.put("userId", userEntity.getUserId());
        userData.put("username", userEntity.getUsername());
        userData.put("email", userEntity.getEmail());
        userData.put("phone", userEntity.getPhone());
        
        for(EulerUserExtraDataProcessor eulerUserExtraDataProcessor : Optional.ofNullable(this.eulerUserExtraDataProcessors).orElse(Collections.emptyList())) {
            Map<String, Object> extraData = eulerUserExtraDataProcessor.loadExtraData(userEntity.getUserId());
            
            if(extraData != null) {
                userData.putAll(extraData);
                break;
            }
            
        }
        
        return userData;
    }
    
    @PostMapping
    public void updataUserProfile(@RequestBody Map<String, Object> data) {
        String userId = UserContext.getCurrentUser().getUserId().toString();
//        String username = (String) data.get("username");
//        Assert.hasText(username, "Required String parameter 'username' is not present");
        
        String email = (String) data.get("email");
        String phone = (String) data.get("phone");

        data.remove("userId");
        data.remove("username");
        data.remove("email");
        data.remove("phone");
        data.remove("password");
        
        this.eulerUserExtraDataService.updateUserWithExtraData(userId, email, phone, data);
    }

}
