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
package org.eulerframework.web.module.authentication.controller.admin.ajax;

import jakarta.annotation.Resource;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.eulerframework.web.core.annotation.AjaxController;
import org.eulerframework.web.core.base.controller.ApiSupportWebController;
import org.eulerframework.web.core.base.request.PageQueryRequest;
import org.eulerframework.web.core.base.response.PageResponse;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.service.admin.UserManageService;

@AjaxController
@RequestMapping("authentication/user")
public class UserManageAjaxContorller extends ApiSupportWebController {
    
    @Resource private UserManageService userManageService;


    @RequestMapping(path="findUserByPage")
    public PageResponse<? extends EulerUserEntity> findUserByPage() {
        return this.userManageService.findUserByPage(new PageQueryRequest(this.getRequest()));
    }
    
    @RequestMapping(path="saveOrUpdateUser", method = RequestMethod.POST)
    public void saveOrUpdateUser(
            @RequestParam(required = false) String userId,
            @RequestParam(required = true) String username,
            @RequestParam(required = true) String email,
            @RequestParam(required = true) String mobile,
            @RequestParam(required = false) String password,
            @RequestParam(required = true) boolean enabled) {
        
        if(!StringUtils.hasText(userId)) {
            this.userManageService.addUser(username, email, mobile, password, enabled, true, true, true);
        } else {
            this.userManageService.updateUser(userId, username, email, mobile, enabled, true, true, true);
        }
    }
    
    @RequestMapping(path="resetPassword", method = RequestMethod.POST)
    public void resetPassword(
            @RequestParam(required = true) String userId,
            @RequestParam(required = true) String newPassword) {
        this.userManageService.updatePassword(userId, newPassword);
    }
    
    @RequestMapping(path="activeUser", method = RequestMethod.POST)
    public void activeUser(
            @RequestParam(required = true) String userId) {
        this.userManageService.activeUser(userId);
    }
    
    @RequestMapping(path="blockUser", method = RequestMethod.POST)
    public void blockUser(
            @RequestParam(required = true) String userId) {
        this.userManageService.blockUser(userId);
    }
}
