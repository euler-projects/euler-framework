package net.eulerframework.web.module.file.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import net.eulerframework.web.core.base.service.impl.BaseService;
import net.eulerframework.web.module.file.dao.IAttachmentDao;
import net.eulerframework.web.module.file.entity.AbstractAttachment;
import net.eulerframework.web.module.file.service.IAttachmentService;

public class AttachmentService extends BaseService implements IAttachmentService {
    
    private Collection<IAttachmentDao<AbstractAttachment>> attachmentDaos;

    public void setAttachmentDaos(Collection<IAttachmentDao<AbstractAttachment>> attachmentDaos) {
        this.attachmentDaos = attachmentDaos;
    }

    @Override
    public void addAttachment(AbstractAttachment attachment) {
        this.getEntityDao(attachmentDaos, attachment.getClass()).save(attachment);
    }

    @Override
    public void updateAttachment(AbstractAttachment attachment) {
        this.getEntityDao(attachmentDaos, attachment.getClass()).saveOrUpdate(attachment);
    }

    @Override
    public void deteteAttachment(AbstractAttachment attachment) {
        this.getEntityDao(attachmentDaos, attachment.getClass()).delete(attachment);
    }

    @Override
    public <T extends AbstractAttachment> T loadAttachment(Serializable ownerId, Class<T> attachmentClass) {
        List<T> data = this.loadAttachments(ownerId, attachmentClass);
        
        if(data == null || data.isEmpty())
            return null;
        else
            return data.get(0);
    }

    @Override
    public <T extends AbstractAttachment> List<T> loadAttachments(Serializable ownerId, Class<T> attachmentClass) {
        @SuppressWarnings("unchecked")
        IAttachmentDao<T> dao = (IAttachmentDao<T>) this.getEntityDao(attachmentDaos, attachmentClass);
        
        return dao.loadByOwnerId(ownerId);
    }

}
