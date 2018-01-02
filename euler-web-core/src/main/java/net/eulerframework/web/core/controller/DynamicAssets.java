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
package net.eulerframework.web.core.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.constant.EulerSysAttributes;
import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;

/**
 * @author cFrost
 *
 */
@WebController
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
        
        this.writeString("var systemAttributes = " + om.writeValueAsString(m));
    }
}
