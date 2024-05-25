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
package org.eulerframework.web.module.oauth2.controller.admin.ajax;

import jakarta.annotation.Resource;

import org.eulerframework.web.core.annotation.AjaxController;
import org.eulerframework.web.core.base.controller.ApiSupportWebController;
import org.eulerframework.web.core.base.request.PageQueryRequest;
import org.eulerframework.web.core.base.response.PageResponse;
import org.eulerframework.web.module.oauth2.entity.EulerOAuth2ClientEntity;
import org.eulerframework.web.module.oauth2.service.EulerOAuth2ClientEntityService;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("oauth2/client")
public class OAuth2ClientManageAjaxController extends ApiSupportWebController {
    
    @Resource
    private EulerOAuth2ClientEntityService eulerOAuth2ClientEntityService;
    
    @RequestMapping("findOAuth2ClientsByPage")
    PageResponse<? extends EulerOAuth2ClientEntity> findClientsByPage() {
        PageQueryRequest pageQueryRequest = new PageQueryRequest(this.getRequest());
        return this.eulerOAuth2ClientEntityService.findClientsByPage(pageQueryRequest);
    }

}
