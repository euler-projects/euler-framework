package net.eulerframework.web.module.authentication.controller.admin.ajax;

import javax.annotation.Resource;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.ApiSupportWebController;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.service.admin.UserManageService;

@AjaxController
@RequestMapping("authentication/user")
public class UserManageAjaxContorller extends ApiSupportWebController {
    
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
