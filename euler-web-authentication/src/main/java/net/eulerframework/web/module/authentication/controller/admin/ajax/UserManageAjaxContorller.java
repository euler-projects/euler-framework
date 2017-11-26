package net.eulerframework.web.module.authentication.controller.admin.ajax;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.core.base.request.PageQueryRequest;
import net.eulerframework.web.core.base.response.PageResponse;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.service.admin.UserManageService;

@AjaxController
@RequestMapping("authentication/user")
public class UserManageAjaxContorller extends AjaxSupportWebController {
    
    @Resource private UserManageService userManageService;


    @RequestMapping(path="findUserByPage")
    public PageResponse<? extends EulerUserEntity> findUserByPage() {
        return this.userManageService.findUserByPage(new PageQueryRequest(this.getRequest(), PageQueryRequest.EASYUI_PAGE_INDEX_NAME, PageQueryRequest.EASYUI_PAGE_SIZE_NAME));
    }
}
