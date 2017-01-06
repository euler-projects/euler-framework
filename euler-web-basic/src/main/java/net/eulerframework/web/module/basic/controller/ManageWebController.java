package net.eulerframework.web.module.basic.controller;

import net.eulerframework.web.core.base.controller.AbstractWebController;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.WebController;

@WebController
@Scope("prototype")
@RequestMapping("/manage")
public class ManageWebController extends AbstractWebController {
}