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
package org.eulerframework.web.module.authentication.controller.settings.account;

import javax.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.eulerframework.web.core.annotation.JspController;
import org.eulerframework.web.core.base.controller.JspSupportWebController;
import org.eulerframework.web.module.authentication.context.UserContext;
import org.eulerframework.web.module.authentication.service.PasswordService;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/settings/account")
public class AccountSettingsWebController extends JspSupportWebController {
    
    public AccountSettingsWebController() {
        super();
        this.setWebControllerName("settings/account");
    }

    @Resource
    private PasswordService passwordService;    

    @RequestMapping(value = "change-password", method = RequestMethod.GET)
    public String changePassword() {
        return this.display("change-password");
    }

    @RequestMapping(value = "change-password", method = RequestMethod.POST)
    public String changePassword(
            @RequestParam(required = true) String oldPassword, 
            @RequestParam(required = true) String newPassword) {
        String userId = UserContext.getCurrentUser().getUserId().toString();
        this.passwordService.updatePassword(userId, oldPassword, newPassword);
        return this.success();
    }
}
