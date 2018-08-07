package net.eulerframework.web.module.authentication.service;

import net.eulerframework.common.util.JavaObjectUtils;
import net.eulerframework.web.module.authentication.entity.EulerUserProfileEntity;

/**
 * @author cFrost
 *
 */
public interface EulerUserProfileService<T extends EulerUserProfileEntity> {

    /**
     * 判断一个用户档案实体类是否可由此实现类处理
     * 
     * @param userProfile 用户档案
     * @return <code>true</code>表示时自己可以处理的用户档案
     */
    default boolean isMyProfile(EulerUserProfileEntity userProfile) {
        return userProfile.getClass().equals(JavaObjectUtils.findSuperInterfaceGenricType(this.getClass(), 0, 0));
    }

    /**
     * 创建用户档案
     * 
     * @param userProfile 用户档案
     * @return 新创建的用户档案实体，与传入参数是同一个实例
     */
    EulerUserProfileEntity createUserProfile(EulerUserProfileEntity userProfile);
    
    T loadUserProfile(String userId);

}
