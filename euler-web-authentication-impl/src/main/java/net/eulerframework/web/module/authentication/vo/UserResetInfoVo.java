package net.eulerframework.web.module.authentication.vo;

import java.util.Date;

import net.eulerframework.web.module.authentication.entity.User;

public class UserResetInfoVo {
    private String id;
    private String username;
    private Date genDate;
    private Date expireDate;
    public UserResetInfoVo() {}
    public UserResetInfoVo(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.genDate = new Date();
        this.expireDate = new Date(this.genDate.getTime() + 1 * 60 * 60 * 1000);
    }
    
    public String getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public Date getGenDate() {
        return genDate;
    }
    public Date getExpireDate() {
        return expireDate;
    }
}
