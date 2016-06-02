package net.eulerform.web.core.security.authentication.task;

import java.lang.reflect.InvocationTargetException;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import net.eulerform.web.core.security.authentication.util.UserContext;

public class SecurityMethodInvokingJobDetailFactoryBean extends MethodInvokingJobDetailFactoryBean {

    @Override
    public Object invoke() throws InvocationTargetException, IllegalAccessException {
        UserContext.addSpecialSystemSecurityContext();
        return super.invoke();
    }
}
