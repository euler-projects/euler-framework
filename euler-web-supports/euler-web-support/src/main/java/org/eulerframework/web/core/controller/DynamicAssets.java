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
package org.eulerframework.web.core.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

import org.eulerframework.web.util.ServletUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eulerframework.constant.EulerSysAttributes;
import org.eulerframework.web.core.annotation.JspController;
import org.eulerframework.web.core.base.controller.JspSupportWebController;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("dynamic-assets")
public class DynamicAssets extends JspSupportWebController {
    
    @Resource ObjectMapper om;

    @ResponseBody
    @RequestMapping(path = "system.js", method = RequestMethod.GET)
    public void systemAttribute() throws JsonProcessingException, IOException {
        this.getResponse().setHeader("Content-Type", "application/javascript");
        
        Map<String, Object> m = new HashMap<>();
        
        Set<String> eulerSysAttributeNames = EulerSysAttributes.getEulerSysAttributeNames();
        
        Enumeration<String> attributeNames = this.getServletContext().getAttributeNames();
        while(attributeNames.hasMoreElements()) {
            String arrtibuteName = attributeNames.nextElement();
            if(eulerSysAttributeNames.contains(arrtibuteName)) {
                m.put(arrtibuteName, this.getServletContext().getAttribute(arrtibuteName));
            }
        }
        
        HttpSession session = this.getRequest().getSession();
        
        if(session != null) {
            attributeNames = this.getRequest().getSession().getAttributeNames();
            while(attributeNames.hasMoreElements()) {
                String arrtibuteName = attributeNames.nextElement();
                if(eulerSysAttributeNames.contains(arrtibuteName)) {
                    m.put(arrtibuteName, this.getRequest().getSession().getAttribute(arrtibuteName));
                }
            }
        }
        
        attributeNames = this.getRequest().getAttributeNames();
        while(attributeNames.hasMoreElements()) {
            String arrtibuteName = attributeNames.nextElement();
            if(eulerSysAttributeNames.contains(arrtibuteName)) {
                m.put(arrtibuteName, this.getRequest().getAttribute(arrtibuteName));
            }
        }
        
        ServletUtils.writeString(this.getResponse(),"var systemAttributes = " + om.writeValueAsString(m));
    }
}
