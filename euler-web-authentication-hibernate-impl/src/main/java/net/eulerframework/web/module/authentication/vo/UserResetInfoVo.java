package net.eulerframework.web.module.authentication.vo;

import java.util.Date;
import java.util.UUID;

import net.eulerframework.common.util.DateUtils;
import net.eulerframework.common.util.jwt.BasicJwtClaims;
import net.eulerframework.web.module.authentication.entity.User;

public class UserResetInfoVo extends BasicJwtClaims {
    private UUID id;
    private String username;
    public UserResetInfoVo() {}
    public UserResetInfoVo(User user, long tokenLifeSecond) {
        this.id = user.getId();
        this.username = user.getUsername();
        Date now = new Date();
        super.setIat(DateUtils.getUnixTimestamp(now));
        super.setExp(super.getIat() + tokenLifeSecond);
    }
    
    public UUID getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
}
