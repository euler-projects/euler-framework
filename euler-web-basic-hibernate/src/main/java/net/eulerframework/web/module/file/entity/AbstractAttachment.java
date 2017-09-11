package net.eulerframework.web.module.file.entity;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@MappedSuperclass
public abstract class AbstractAttachment extends UUIDEntity<AbstractAttachment> {
    public abstract void setOwnerId(Serializable ownerId);
    public abstract Serializable getOwnerId();
}
