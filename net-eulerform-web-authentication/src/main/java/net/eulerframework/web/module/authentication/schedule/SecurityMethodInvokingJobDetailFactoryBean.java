package net.eulerframework.web.module.authentication.schedule;

import java.lang.reflect.InvocationTargetException;

import net.eulerframework.web.module.authentication.util.UserContext;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

public class SecurityMethodInvokingJobDetailFactoryBean extends MethodInvokingJobDetailFactoryBean {

    @Override
    public Object invoke() throws InvocationTargetException, IllegalAccessException {
        UserContext.addSpecialSystemSecurityContext();
        return super.invoke();
    }
}
