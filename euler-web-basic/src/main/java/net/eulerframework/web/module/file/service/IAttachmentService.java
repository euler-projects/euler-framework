package net.eulerframework.web.module.file.service;

import java.io.Serializable;
import java.util.List;

import net.eulerframework.web.core.base.service.IBaseService;
import net.eulerframework.web.module.file.entity.AbstractAttachment;

public interface IAttachmentService extends IBaseService {

    public void addAttachment(AbstractAttachment attachment);
    
    public void updateAttachment(AbstractAttachment attachment);
    
    public void deteteAttachment(AbstractAttachment attachment);

    public <T extends AbstractAttachment> T loadAttachment(Serializable ownerId, Class<T> attachmentClass);
    
    public <T extends AbstractAttachment> List<T> loadAttachments(Serializable ownerId, Class<T> attachmentClass);
}
