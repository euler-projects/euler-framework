/**
 * 
 */
package net.eulerframework.web.module.authentication.api;

import javax.annotation.Resource;
import javax.validation.Valid;

import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.eulerframework.common.util.StringTool;
import net.eulerframework.web.core.annotation.ApiEndpoint;
import net.eulerframework.web.core.base.controller.AbstractApiEndpoint;
import net.eulerframework.web.core.base.response.HttpStatusResponse;
import net.eulerframework.web.core.base.response.WebServiceResponse;
import net.eulerframework.web.module.authentication.entity.User;
import net.eulerframework.web.module.authentication.exception.UserSignUpException;
import net.eulerframework.web.module.authentication.service.IAuthenticationService;

/**
 * @author cFrost
 *
 */
@ApiEndpoint
@Scope("prototype")
@RequestMapping("/")
public class AuthenticationApi extends AbstractApiEndpoint {

    @Resource
    private IAuthenticationService authenticationService;

    @RequestMapping(value = "litesignup", method = RequestMethod.POST)
    public WebServiceResponse<String> litesignup(@Valid User user) {
        String userId = this.authenticationService.signUp(user);

        if (!StringTool.isNull(userId)) {
            return new WebServiceResponse<>(userId);
        } else
            throw new UserSignUpException("Unknown user sign up error");
    }
    
    /**  
     * 用于在程序发生{@link UserSignUpException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({UserSignUpException.class})   
    public Object exception(UserSignUpException e) {
        e.printStackTrace();
        return new HttpStatusResponse(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
    }

}
