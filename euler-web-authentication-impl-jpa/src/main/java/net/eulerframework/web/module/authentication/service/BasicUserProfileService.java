package net.eulerframework.web.module.authentication.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.authentication.entity.BasicUserProfile;
import net.eulerframework.web.module.authentication.entity.EulerUserProfileEntity;
import net.eulerframework.web.module.authentication.repository.BasicUserProfileRepository;

/**
 * @author cFrost
 *
 */
@Service
public class BasicUserProfileService extends BaseService implements EulerUserProfileService<BasicUserProfile> {
    
    @Resource private BasicUserProfileRepository basicUserProfileRepository;

    @Override
    public BasicUserProfile createUserProfile(EulerUserProfileEntity userProfile) {
        BasicUserProfile basicUserProfile = (BasicUserProfile) userProfile;
        this.basicUserProfileRepository.save(basicUserProfile);
        return basicUserProfile;
    }

    @Override
    public BasicUserProfile loadUserProfile(String userId) {
        // TODO Auto-generated method stub
        return null;
    }

}
