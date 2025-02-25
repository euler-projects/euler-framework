package org.eulerframework.web.core.base.controller;

import jakarta.servlet.ServletContext;
import org.eulerframework.common.util.Assert;
import org.eulerframework.constant.EulerSysAttributes;
import org.eulerframework.web.util.ServletUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ThymeleafPageRender implements PageRender {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static volatile Map<String, Object> TEMPLATE_ATTRIBUTES = null;
    private static final Object TEMPLATE_ATTRIBUTES_LOCK = new Object();

    private String viewPathPrefix = "";

    public void setViewPathPrefix(String viewPathPrefix) {
        this.viewPathPrefix = viewPathPrefix;
    }

    @Override
    public ModelAndView display(String view, String theme, final Map<String, Object> attributes) {
        Assert.notNull(view, "view is empty");

        // ensure view not start with a '/'
        while (view.startsWith("/")) {
            view = view.substring(1);
        }

        String prefixedView;
        if (StringUtils.hasText(theme)) {
            prefixedView = "theme/" + theme + "/" + this.viewPathPrefix + view;
        } else {
            prefixedView = this.viewPathPrefix + view;
        }
        this.logger.trace("display view '{}'", prefixedView);

        Map<String, Object> model = this.newModelInstance();
        if(!CollectionUtils.isEmpty(attributes)) {
            model.putAll(attributes);
        }

        return new ModelAndView(prefixedView, model);
    }

    private Map<String, Object> newModelInstance() {
        if (TEMPLATE_ATTRIBUTES == null) {
            synchronized (TEMPLATE_ATTRIBUTES_LOCK) {
                if (TEMPLATE_ATTRIBUTES == null) {
                    TEMPLATE_ATTRIBUTES = Map.of("ctx", getEulerAttributesContext());
                }
            }
        }

        Map<String, Object> model = new HashMap<>();
        model.put("euler", TEMPLATE_ATTRIBUTES);

        return model;
    }

    private Map<String, Object> getEulerAttributesContext() {
        ServletContext servletContext = ServletUtils.getServletContext();
        Set<String> eulerSysAttributeNames = EulerSysAttributes.getEulerSysAttributeNames();
        Map<String, Object> context = new HashMap<>();
        for (String eulerSysAttributeName : eulerSysAttributeNames) {
            Object value = servletContext.getAttribute(eulerSysAttributeName);
            if (value != null) {
                context.put(eulerSysAttributeName, value);
            }
        }
        return context;
    }
}
