package net.eulerframework.web.module.authentication.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import net.eulerframework.web.core.base.entity.NonIDEntity;

@Entity
@Table(name = "sys_authority")
public class Authority extends NonIDEntity<Authority, String> implements EulerAuthorityEntity {

    @Id
    @NotNull
    @Pattern(regexp = "[A-Z][A-Z_]*", message = "{validation.authority.authority}")
    @Column(name = "authority")
    private String authority;
    @NotNull
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "description")
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

    @Override
    public String getId() {
        return this.authority;
    }

    @Override
    public void setId(String id) {
        this.authority = id;
    }

    @Override
    public int compareTo(Authority o) {
        return this.getId().compareTo(o.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((authority == null) ? 0 : authority.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Authority other = (Authority) obj;
        if (authority == null) {
            if (other.authority != null)
                return false;
        } else if (!authority.equals(other.authority))
            return false;
        return true;
    }

}
