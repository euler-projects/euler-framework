/**
 * 
 */
package net.eulerframework.web.module.authentication.controller.ajax;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import net.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("/")
public class AuthenticationAjaxController extends AjaxSupportWebController {

    @RequestMapping(path="validUsername", method = RequestMethod.GET)
    public void validUsername(@RequestParam String username) throws UserInfoCheckWebException {
        UserDataValidator.validUsername(username);
    }

    @RequestMapping(path="validEmail", method = RequestMethod.GET)
    public void validEmail(@RequestParam String email) throws UserInfoCheckWebException {
        UserDataValidator.validEmail(email);
    }

    @RequestMapping(path="validMobile", method = RequestMethod.GET)
    public void validMobile(@RequestParam String mobile) throws UserInfoCheckWebException {
        UserDataValidator.validMobile(mobile);
    }

    @RequestMapping(path="validPassword", method = RequestMethod.GET)
    public void validPassword(@RequestParam String password) throws UserInfoCheckWebException {
        UserDataValidator.validPassword(password);
    }

}
