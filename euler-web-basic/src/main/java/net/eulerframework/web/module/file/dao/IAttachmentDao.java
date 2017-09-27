package net.eulerframework.web.module.file.dao;

import java.io.Serializable;
import java.util.List;

import net.eulerframework.web.core.base.dao.IBaseDao;
import net.eulerframework.web.module.file.entity.AbstractAttachment;

public interface IAttachmentDao<T extends AbstractAttachment> extends IBaseDao<T> {

    public List<T> loadByOwnerId(Serializable ownerId);
}
