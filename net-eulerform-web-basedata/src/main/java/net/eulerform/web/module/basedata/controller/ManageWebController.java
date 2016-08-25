package net.eulerform.web.module.basedata.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerform.web.core.annotation.WebController;
import net.eulerform.web.core.base.controller.BaseController;

@WebController
@Scope("prototype")
@RequestMapping("/manage")
public class ManageWebController extends BaseController {
}
