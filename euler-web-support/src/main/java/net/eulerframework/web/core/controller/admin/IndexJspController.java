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
package net.eulerframework.web.core.controller.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;

@JspController
@RequestMapping("/")
public class IndexJspController extends JspSupportWebController {
    
    @RequestMapping(value={""}, method = RequestMethod.GET)
    public String index() {
        return this.display("index");
    }
    
    /**
     * 捕获所有未定义的请求，只有满足[contextPaht]/AdminRootPath才会被重定向到[contextPaht]/AdminRootPath/，其他请求返回404
     * @return 后台管理首页
     */
//    @RequestMapping(value={"**"}, method = RequestMethod.GET)
//    public String adminRedirect() {
//        if(this.getRequest().getRequestURI().replace(this.getRequest().getContextPath(), "").equals(WebConfig.getAdminRootPath()))
//            return this.redirect(WebConfig.getAdminRootPath() + "/");
//        throw new PageNotFoundException(this.getRequest());
//    }
}
