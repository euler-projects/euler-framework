package net.eulerframework.web.module.authentication.controller.ajax.settings.profile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import net.eulerframework.web.core.annotation.AjaxController;
import net.eulerframework.web.core.base.controller.ApiSupportWebController;
import net.eulerframework.web.module.authentication.context.UserContext;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.service.EulerUserExtraDataProcessor;
import net.eulerframework.web.module.authentication.service.EulerUserExtraDataService;

/**
 * @author cFrost
 *
 */
@AjaxController
@RequestMapping("settings")
public class ProfileSettingsAjaxController extends ApiSupportWebController {

    @Autowired
    private EulerUserExtraDataService eulerUserExtraDataService;
    @Autowired(required = false) 
    private List<EulerUserExtraDataProcessor> eulerUserExtraDataProcessors;
    
    @GetMapping("profile")
    public Map<String, Object> findUserProfile() {
        EulerUserEntity userEntity = UserContext.getCurrentUserEntity();
        
        Map<String, Object> userData = new HashMap<>();

        userData.put("userId", userEntity.getUserId());
        userData.put("username", userEntity.getUsername());
        userData.put("email", userEntity.getEmail());
        userData.put("mobile", userEntity.getMobile());
        
        for(EulerUserExtraDataProcessor eulerUserExtraDataProcessor : this.eulerUserExtraDataProcessors) {
            Map<String, Object> extraData = eulerUserExtraDataProcessor.loadExtraData(userEntity.getUserId());
            
            if(extraData != null) {
                userData.putAll(eulerUserExtraDataProcessor.loadExtraData(userEntity.getUserId()));
                break;
            }
            
        }
        
        return userData;
    }
    
    @PostMapping("profile")
    public void updataUserProfile(@RequestBody Map<String, Object> data) {
        String userId = UserContext.getCurrentUser().getUserId().toString();
//        String username = (String) data.get("username");
//        Assert.hasText(username, "Required String parameter 'username' is not present");
        
        String email = (String) data.get("email");
        String mobile = (String) data.get("mobile");

        data.remove("userId");
        data.remove("username");
        data.remove("email");
        data.remove("mobile");
        data.remove("password");
        
        this.eulerUserExtraDataService.updateUserWithExtraData(userId, email, mobile, data);
    }

}
