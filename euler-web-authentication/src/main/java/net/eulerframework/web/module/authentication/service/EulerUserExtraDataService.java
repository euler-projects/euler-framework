package net.eulerframework.web.module.authentication.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.exception.UserInfoCheckWebException;
import net.eulerframework.web.module.authentication.util.UserDataValidator;

/**
 * @author cFrost
 *
 */
@Service
public class EulerUserExtraDataService {

    @Autowired
    private EulerUserEntityService eulerUserEntityService;
    @Autowired(required = false) 
    private List<EulerUserExtraDataProcessor> eulerUserExtraDataProcessors;

    public void updateUserWithExtraData(
            String userId, 
            //String newUsername, 
            String newEmail, 
            String newMobile,
            Map<String, Object> extraData) 
            throws UserInfoCheckWebException {
        EulerUserEntity user = this.eulerUserEntityService.loadUserByUserId(userId);
        
//        UserDataValidator.validUsername(newUsername);
//        user.setUsername(newUsername.trim());
        
        if(StringUtils.hasText(newEmail)) {
            if (!newEmail.equalsIgnoreCase(user.getEmail())) {
                UserDataValidator.validEmail(newEmail);
                user.setEmail(newEmail.trim());
            }
        } else {
            user.setEmail(null);
        }
        
        if(StringUtils.hasText(newMobile)) { 
            if(!newMobile.equalsIgnoreCase(user.getMobile())) {
                UserDataValidator.validMobile(newMobile);
                user.setMobile(newMobile.trim());
            }
        } else {
            user.setMobile(null);
        }
        
        this.eulerUserEntityService.updateUser(user);
        
        if(extraData != null && !extraData.isEmpty() && this.eulerUserExtraDataProcessors != null) {
            for(EulerUserExtraDataProcessor eulerUserExtraDataProcessor : this.eulerUserExtraDataProcessors) {
                if(eulerUserExtraDataProcessor.process(userId, extraData)) {
                    break;
                }
            }
        }
    }
}
