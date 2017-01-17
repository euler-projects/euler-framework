package net.eulerframework.web.module.authentication.schedule;

import java.lang.reflect.InvocationTargetException;

import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import net.eulerframework.web.module.authentication.util.UserContext;

public class SecurityMethodInvokingJobDetailFactoryBean extends MethodInvokingJobDetailFactoryBean {

    @Override
    public Object invoke() throws InvocationTargetException, IllegalAccessException {
        UserContext.sudo();
        return super.invoke();
    }
}
