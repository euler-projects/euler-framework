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
 * https://github.com/euler-projects/euler-framework
 * https://cfrost.net
 */
package net.eulerframework.web.module.authentication.controller.admin.ajax;

import javax.annotation.Resource;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.htservice.admin.UserManageService;

@AjaxController
@RequestMapping("authentication/user")
public class UserManageAjaxContorller extends AjaxSupportWebController {
    
    @Resource private UserManageService userManageService;


    @RequestMapping(path="findUserByPage")
    public PageResponse<? extends EulerUserEntity> findUserByPage() {
        return this.userManageService.findUserByPage(new PageQueryRequest(this.getRequest(), PageQueryRequest.EASYUI_PAGE_INDEX_NAME, PageQueryRequest.EASYUI_PAGE_SIZE_NAME));
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
