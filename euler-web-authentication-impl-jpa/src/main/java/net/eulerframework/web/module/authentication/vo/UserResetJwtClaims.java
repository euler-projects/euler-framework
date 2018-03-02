package net.eulerframework.web.module.authentication.vo;

import java.util.Date;

import net.eulerframework.common.util.DateUtils;
import net.eulerframework.common.util.jwt.BasicJwtClaims;
import net.eulerframework.web.module.authentication.entity.EulerUserEntity;

public class UserResetJwtClaims extends BasicJwtClaims {
    private String userId;
    public UserResetJwtClaims() {}
    public UserResetJwtClaims(EulerUserEntity eulerUserEntity, long tokenLifeSecond) {
        this.userId = eulerUserEntity.getUserId();
        Date now = new Date();
        super.setIat(DateUtils.getUnixTimestamp(now));
        super.setExp(super.getIat() + tokenLifeSecond);
    }
    
    public String getUserId() {
        return userId;
    }
}
