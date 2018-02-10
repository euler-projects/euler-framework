package net.eulerframework.web.module.oldauthentication.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import net.eulerframework.constant.EulerSysAttributes;
import net.eulerframework.web.module.oldauthentication.util.Captcha;

public class CaptchaUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    
    private boolean enableCaptcha = true;
    
    public void setEnableCaptcha(boolean enableCaptcha) {
        this.enableCaptcha = enableCaptcha;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        String realCaptcha= "";
        String userCaptcha="";
        
        if(enableCaptcha) {
            realCaptcha = Captcha.getRealCaptcha(request);
            userCaptcha = request.getParameter("captcha");            
        }
        
        if(!enableCaptcha || (StringUtils.hasText(realCaptcha) && realCaptcha.equalsIgnoreCase(userCaptcha))) {
            Authentication result = super.attemptAuthentication(request, response);
            request.getSession().setAttribute(EulerSysAttributes.USER_INFO.value(), result.getPrincipal());
            return result;
        } else {
            throw new InternalAuthenticationServiceException("captcha_error");
        }
    }

}
