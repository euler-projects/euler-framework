package net.eulerframework.web.module.authentication.service;

import javax.servlet.http.HttpServletRequest;

import net.eulerframework.web.module.authentication.exception.NotSupportRobotCheckRequestException;

public interface RobotCheckService {

    boolean isRobot(HttpServletRequest request) throws NotSupportRobotCheckRequestException;
}
