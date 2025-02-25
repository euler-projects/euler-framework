package org.eulerframework.web.core.base.controller;

import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

public interface PageRender {
    ModelAndView display(String view, String theme, Map<String, Object> attributes);
}
