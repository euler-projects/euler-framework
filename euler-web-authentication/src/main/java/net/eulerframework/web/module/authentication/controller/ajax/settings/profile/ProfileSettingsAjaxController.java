/**
 * 
 */
package net.eulerframework.web.module.authentication.controller.ajax.settings.profile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.AjaxSupportWebController;

/**
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("/settings/profile")
public class ProfileSettingsAjaxController extends AjaxSupportWebController {
    
    @GetMapping
    public void findUserProfile() {
        
    }
    
    @PostMapping
    public void updataUserProfile() {
        
    }

}
