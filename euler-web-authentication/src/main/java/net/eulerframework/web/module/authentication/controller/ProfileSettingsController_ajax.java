/**
 * 
 */
package net.eulerframework.web.module.authentication.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.eulerframework.web.core.annotation.JspController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;
import net.eulerframework.web.core.base.response.easyuisupport.EasyUIAjaxResponse;
import net.eulerframework.web.core.exception.web.UndefinedWebException;
import net.eulerframework.web.module.authentication.context.UserContext;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.UserService;

/**
 * @author cFrost
 *
 */
@JspController
@RequestMapping("/settings/profile")
public class ProfileSettingsController_ajax extends AjaxSupportWebController {

    @Resource
    private UserService userService;
    
    @ResponseBody
    @RequestMapping(value = {"updateUserAvatar_ajax"}, method = RequestMethod.POST)
    public EasyUIAjaxResponse<String> updateUserAvatar(
            @RequestParam String avatarFileId) {
        try {
            this.userService.updateAvatar(UserContext.getCurrentUser().getId(), avatarFileId);
        } catch (UserNotFoundException e) {
            throw new UndefinedWebException(e.getMessage(), e);
        }
        return EasyUIAjaxResponse.SUCCESS_RESPONSE;
    }

}
