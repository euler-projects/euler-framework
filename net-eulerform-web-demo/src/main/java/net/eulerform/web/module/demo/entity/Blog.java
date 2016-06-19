package net.eulerform.web.module.demo.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import net.eulerform.web.core.base.entity.IDLogicDelEntity;

@SuppressWarnings("serial")
@Entity
@XmlRootElement
@Table(name="BLG_BLOG")
public class Blog extends IDLogicDelEntity<Blog> {
    
    private String name;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
