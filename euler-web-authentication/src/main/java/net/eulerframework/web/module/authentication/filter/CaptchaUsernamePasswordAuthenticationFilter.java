package net.eulerframework.web.module.authentication.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import net.eulerframework.web.module.authentication.util.Captcha;

public class CaptchaUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    
    private boolean enableCaptcha = true;
    
    public void setEnableCaptcha(boolean enableCaptcha) {
        this.enableCaptcha = enableCaptcha;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
            HttpServletResponse response) throws AuthenticationException {
        String captcha= "";
        String captchaUser="";
        
        if(enableCaptcha) {
            captcha = String.valueOf(request.getSession().getAttribute(Captcha.RANDOMCODEKEY));
            captchaUser = request.getParameter("captcha");            
        }
        
        if(!enableCaptcha || (StringUtils.hasText(captcha) && captcha.equalsIgnoreCase(captchaUser))) {
            Authentication result = super.attemptAuthentication(request, response);
            request.getSession().setAttribute("__USERINFO", result.getPrincipal());
            return result;
        } else {
            throw new InternalAuthenticationServiceException("captcha_error");
        }
    }

}
