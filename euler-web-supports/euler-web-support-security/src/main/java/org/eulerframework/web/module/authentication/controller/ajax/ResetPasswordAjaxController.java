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
package org.eulerframework.web.module.authentication.controller.ajax;

import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import org.eulerframework.web.core.annotation.AjaxController;
import org.eulerframework.web.core.base.controller.ApiSupportWebController;
import org.eulerframework.web.module.authentication.service.PasswordService;

/**
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("/")
public class ResetPasswordAjaxController extends ApiSupportWebController {

    @Resource
    private PasswordService passwordService;  

    @RequestMapping(value = "reset-password-email-sms", method = RequestMethod.POST)
    public void getPasswordResetSMS(@RequestParam String mobile) {
        this.passwordService.passwdResetSMSGen(mobile);
    }

}
