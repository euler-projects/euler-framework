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
import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.JspSupportWebController;

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
        
        this.writeString("var systemAttributes = " + om.writeValueAsString(m));
    }
}
