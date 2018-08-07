/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
