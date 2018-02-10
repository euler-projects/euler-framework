package net.eulerframework.web.module.oldauthentication.schedule;

import java.lang.reflect.InvocationTargetException;

import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import net.eulerframework.web.module.oldauthentication.context.UserContext;

public class SecurityMethodInvokingJobDetailFactoryBean extends MethodInvokingJobDetailFactoryBean {

    @Override
    public Object invoke() throws InvocationTargetException, IllegalAccessException {
        UserContext.sudo();
        return super.invoke();
    }
}
