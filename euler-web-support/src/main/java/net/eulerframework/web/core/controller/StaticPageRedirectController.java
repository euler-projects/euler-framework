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
package net.eulerframework.web.core.controller;

import java.util.Locale;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.common.util.CommonUtils;
import net.eulerframework.web.config.WebConfig;
import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;

@JspController
@RequestMapping("/")
public class StaticPageRedirectController extends JspSupportWebController {
    
    @RequestMapping("h/{url}")
    public String error(@PathVariable("url") String url, Locale locale) {
        
        String queryString = this.getRequest().getQueryString();
        
        if(StringUtils.hasText(queryString)) {
            url = url + "?" + queryString;
        }
        
        return this.redirect(WebConfig.getStaticPagesRootPath() + "/" + CommonUtils.formatLocal(locale, '-') + "/" + url);
    }
    
}
