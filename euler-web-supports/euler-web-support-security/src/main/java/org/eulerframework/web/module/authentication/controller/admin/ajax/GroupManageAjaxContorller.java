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
package org.eulerframework.web.module.authentication.controller.admin.ajax;

import org.eulerframework.web.core.annotation.AjaxController;
import org.eulerframework.web.core.base.controller.ApiSupportWebController;
import org.eulerframework.web.core.base.request.PageQueryRequest;
import org.eulerframework.web.core.base.response.PageResponse;
import org.eulerframework.web.module.authentication.context.UserContext;
import org.eulerframework.web.module.authentication.entity.EulerGroupEntity;
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.service.admin.GroupManageService;
import org.eulerframework.web.module.authentication.service.admin.UserManageService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;

@AjaxController
@RequestMapping("authentication/group")
public class GroupManageAjaxContorller extends ApiSupportWebController {
    
    @Resource private GroupManageService groupManageService;


    @RequestMapping(path="findGroupByPage")
    public PageResponse<? extends EulerGroupEntity> findGroupByPage() {
        return this.groupManageService.findGroupByPage(new PageQueryRequest(this.getRequest()));
    }

    @RequestMapping(path="findUserGroupByPage")
    public PageResponse<? extends EulerGroupEntity> findUserGroupByPage() {
        return this.groupManageService.findUserGroupByPage(new PageQueryRequest(this.getRequest()));
    }
}
