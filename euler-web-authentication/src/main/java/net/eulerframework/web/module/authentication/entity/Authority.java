package net.eulerframework.web.module.authentication.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.springframework.security.core.GrantedAuthority;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name="SYS_AUTHORITY")
public class Authority extends UUIDEntity<Authority> implements GrantedAuthority {
   
    public final static String ANONYMOUS = "ANONYMOUS";
    public final static String ROOT = "ROOT";
    
    public final static Authority ROOT_AUTHORITY;
    
    static {
        ROOT_AUTHORITY = new Authority();
        ROOT_AUTHORITY.setAuthority(ROOT);
        ROOT_AUTHORITY.setDescription(ROOT);
    }

    @NotNull
    @Column(name="NAME",nullable=false,unique=true)
    private String name;
    @NotNull
    @Pattern(regexp="[A-Z][A-Z_]*", message="{validation.authority.authority}")
    @Column(name="AUTHORITY",nullable=false,unique=true)
    private String authority;
    @Column(name="DESCRIPTION")
    private String description;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
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
