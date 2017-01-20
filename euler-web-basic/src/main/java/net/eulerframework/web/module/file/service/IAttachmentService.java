package net.eulerframework.web.module.file.service;

import java.io.Serializable;
import java.util.List;

import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.file.entity.AbstractAttachment;
import net.eulerframework.web.module.file.exception.AttachmentNotFoundException;

public interface IAttachmentService extends IBaseService {

    public void addAttachment(AbstractAttachment attachment);
    
    public void updateAttachment(AbstractAttachment attachment);
    
    public void deleteAttachment(AbstractAttachment attachment);
    
    public <T extends AbstractAttachment> void deleteAttachment(Serializable ownerId, String attachmentId, Class<T> attachmentClass) throws AttachmentNotFoundException;
    
    public <T extends AbstractAttachment> void deleteAllAttachment(Serializable ownerId, Class<T> attachmentClass);

    public <T extends AbstractAttachment> T loadAttachment(Serializable ownerId, Class<T> attachmentClass);
    
    public <T extends AbstractAttachment> List<T> loadAttachments(Serializable ownerId, Class<T> attachmentClass);
}
