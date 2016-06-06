package net.eulerform.web.module.authentication.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

import net.eulerform.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name="SYS_AUTHORITY")
public class Authority extends UUIDEntity<Authority> implements GrantedAuthority {
   
    public final static String ANONYMOUS = "ANONYMOUS";
    public final static String SYSTEM = "SYSTEM";
    
    public final static Authority SYSTEM_AUTHORITY;
    
    static {
        SYSTEM_AUTHORITY = new Authority();
        SYSTEM_AUTHORITY.setAuthority(SYSTEM);
        SYSTEM_AUTHORITY.setDescription(SYSTEM);
    }

    @Column(name="AUTHORITY",nullable=false,unique=true)
    private String authority;
    @Column(name="DESCRIPTION")
    private String description;
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setAuthority(String authority) {
        this.authority = authority;
    }
    @Override
    public String getAuthority() {
        return authority;
    }    
}
