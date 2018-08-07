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
package net.eulerframework.web.module.authentication.controller.admin;

import javax.annotation.Resource;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;
import net.eulerframework.web.module.authentication.service.RootService;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("passwd")
public class PasswdJspController extends JspSupportWebController {
    
    @Resource
    private RootService rootService;
    @Resource
    private UserDetailsService userDetailsService;
    
    @RequestMapping(value = { "root" }, method = RequestMethod.GET)
    public String resetRootPwd() { 
        try {
            //UserContext.sudo();
            this.rootService.resetRootPassword();
        } catch (Exception e) {
            //DO_NOTHING            
        }
        
        return this.notfound();
    }
    
    @RequestMapping(value = { "/admin" }, method = RequestMethod.GET)
    public String resetAdminPwd() { 
        try {
            //UserContext.sudo();
            this.rootService.resetAdminPassword();
        } catch (Exception e) {
            //DO_NOTHING            
        }
        
        return this.notfound();
    }

}
