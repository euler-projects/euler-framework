/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.WebController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.core.base.response.AjaxResponse;
import net.eulerframework.web.core.exception.web.DefaultAjaxException;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.UserService;
import net.eulerframework.web.module.authentication.util.UserContext;

/**
 * @author cFrost
 *
 */
@WebController
@RequestMapping("/settings/profile")
public class ProfileSettingsController_ajax extends AjaxSupportWebController {

    @Resource
    private UserService userService;
    
    @ResponseBody
    @RequestMapping(value = {"updateUserAvatar_ajax"}, method = RequestMethod.POST)
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
