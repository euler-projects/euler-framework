/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.core.base.response.AjaxResponse;
import net.eulerframework.web.core.exception.web.DefaultAjaxException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.IAuthenticationService;
import net.eulerframework.web.module.authentication.service.UserService;
import net.eulerframework.web.module.authentication.util.UserContext;

/**
 * @author cFrost
 *
 */
@WebController
@RequestMapping("/")
public class AuthenticationAjaxWebController extends AjaxSupportWebController {

    @Resource
    private IAuthenticationService authenticationService;
    @Resource
    private UserService userService;

    @ResponseBody
    @RequestMapping(value = "getPasswordResetSMS_ajax", method = RequestMethod.POST)
    public AjaxResponse<String> getPasswordResetSMS(@RequestParam String mobile) {
        this.authenticationService.passwdResetSMSGen(mobile);
        return AjaxResponse.SUCCESS_RESPONSE;
    }
    
    @ResponseBody
    @PreAuthorize("isFullyAuthenticated()")
    @RequestMapping(value = "updateUserAvatar_ajax", method = RequestMethod.POST)
    public AjaxResponse<String> updateUserAvatar(
            @RequestParam String avatarFileId) {
        try {
            this.userService.updateAvatar(UserContext.getCurrentUser().getId(), avatarFileId);
        } catch (UserNotFoundException e) {
            throw new DefaultAjaxException(e.getMessage(), e);
        }
        return AjaxResponse.SUCCESS_RESPONSE;
    }

}
