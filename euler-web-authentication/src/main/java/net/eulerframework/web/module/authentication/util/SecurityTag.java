package net.eulerframework.web.module.authentication.util;

import org.springframework.util.StringUtils;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;
import net.eulerframework.web.module.authentication.exception.UserNotFoundException;
import net.eulerframework.web.module.authentication.service.EulerUserEntityService;

/**
 * @author cFrost
 *
 */
public abstract class SecurityTag {

    private static final DefaultObjectCache<String, EulerUserEntity> USER_ID_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(60_000);

    private static EulerUserEntityService eulerUserEntityService;
    
    public static void setEulerUserEntityService(EulerUserEntityService eulerUserEntityService) {
        SecurityTag.eulerUserEntityService = eulerUserEntityService;
    }
    
    public static String userIdtoUserame(String userId) {
        if(!StringUtils.hasText(userId)) {
            return "-";
        }
        
        EulerUserEntity user = USER_ID_CAHCE.get(userId, key -> {
            try {
                return eulerUserEntityService.loadUserByUserId(userId);
            } catch (UserNotFoundException e) {
                return null;
            }
        });
        
        return user == null ? "-" : user.getUsername();
    }

}
