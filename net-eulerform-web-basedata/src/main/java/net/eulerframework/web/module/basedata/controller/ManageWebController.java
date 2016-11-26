package net.eulerframework.web.module.basedata.controller;

import net.eulerframework.web.core.base.controller.DefaultWebController;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.WebController;

@WebController
@Scope("prototype")
@RequestMapping("/manage")
public class ManageWebController extends DefaultWebController {
}
