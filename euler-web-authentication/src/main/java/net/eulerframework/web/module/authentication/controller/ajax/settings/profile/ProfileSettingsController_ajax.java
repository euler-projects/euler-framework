/**
 * 
 */
package net.eulerframework.web.module.authentication.controller.ajax.settings.profile;

import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;

/**
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("/settings/profile")
public class ProfileSettingsController_ajax extends AjaxSupportWebController {

//    @Resource
//    private UserService userService;
//    
//    @ResponseBody
//    @RequestMapping(value = {"updateUserAvatar_ajax"}, method = RequestMethod.POST)
//    public EasyUIAjaxResponse<String> updateUserAvatar(
//            @RequestParam String avatarFileId) {
//        try {
//            this.userService.updateAvatar(UserContext.getCurrentUser().getId(), avatarFileId);
//        } catch (UserNotFoundException e) {
//            throw new UndefinedWebException(e.getMessage(), e);
//        }
//        return EasyUIAjaxResponse.SUCCESS_RESPONSE;
//    }

}
