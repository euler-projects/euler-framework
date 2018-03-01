package net.eulerframework.web.module.file.htservice;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.eulerframework.common.util.Assert;
import net.eulerframework.web.core.base.htservice.impl.BaseHibernateSupportService;
import net.eulerframework.web.module.file.dao.IAttachmentDao;
import net.eulerframework.web.module.file.entity.AbstractAttachment;
import net.eulerframework.web.module.file.entity.AbstractFileAttachment;
import net.eulerframework.web.module.file.exception.AttachmentNotFoundException;

public class AttachmentService extends BaseHibernateSupportService {

    private Collection<IAttachmentDao<AbstractAttachment>> attachmentDaos;

    public void setAttachmentDaos(Collection<IAttachmentDao<AbstractAttachment>> attachmentDaos) {
        this.attachmentDaos = attachmentDaos;
    }

    public void addAttachment(AbstractAttachment attachment) {
        Assert.notNull(attachment, "attachment is null");
        Assert.notNull(attachment.getOwnerId(), "attachment owner id is null");

        this.getEntityDao(attachmentDaos, attachment.getClass()).save(attachment);
    }

    public void updateAttachment(AbstractAttachment attachment) {
        Assert.notNull(attachment, "attachment is null");
        Assert.notNull(attachment.getOwnerId(), "attachment owner id is null");

        this.getEntityDao(attachmentDaos, attachment.getClass()).saveOrUpdate(attachment);
    }

    public void deleteAttachment(AbstractAttachment attachment) {
        Assert.notNull(attachment, "attachment is null");
        Assert.notNull(attachment.getOwnerId(), "attachment owner id is null");

        this.getEntityDao(attachmentDaos, attachment.getClass()).deleteById(attachment.getId());
    }

    public <T extends AbstractAttachment> void deleteAttachment(Serializable ownerId, String attachmentId,
            Class<T> attachmentClass) throws AttachmentNotFoundException {
        Assert.notNull(ownerId, "attachment owner id is null");
        Assert.notNull(attachmentId, "attachment id is null");

        @SuppressWarnings("unchecked")
        IAttachmentDao<T> dao = (IAttachmentDao<T>) this.getEntityDao(attachmentDaos, attachmentClass);

        T data = dao.load(attachmentId);

        if (data == null || !data.getOwnerId().equals(ownerId))
            throw new AttachmentNotFoundException(
                    "Attachment id is " + attachmentId + " and owner id is " + ownerId + " not found");

        dao.deleteById(attachmentId);

        if (AbstractFileAttachment.class.isAssignableFrom(attachmentClass)) {
            String fileId = ((AbstractFileAttachment) data).getFileId();
            System.out.println("delete archived file " + fileId);
            // TODO: delete archived file
        }
    }

    public <T extends AbstractAttachment> void deleteAllAttachment(Serializable ownerId, Class<T> attachmentClass) {
        Assert.notNull(ownerId, "attachment owner id is null");

        List<T> data = this.loadAttachments(ownerId, attachmentClass);

        if (data == null || data.isEmpty())
            return;

        @SuppressWarnings("unchecked")
        IAttachmentDao<T> dao = (IAttachmentDao<T>) this.getEntityDao(attachmentDaos, attachmentClass);
        
        Set<Serializable> ids = new HashSet<>();
        for(T each : data) {
            ids.add(each.getId());
        }
        dao.deleteByIds(ids);

        if (AbstractFileAttachment.class.isAssignableFrom(attachmentClass)) {
            Set<String> fileIdSet = new HashSet<>();
            for (AbstractAttachment each : data) {
                fileIdSet.add(((AbstractFileAttachment) each).getFileId());
            }
            if (!fileIdSet.isEmpty()) {
                System.out.println("delete archived file " + fileIdSet);
                // TODO: delete archived file
            }
        }
    }

    public <T extends AbstractAttachment> T loadAttachment(Serializable ownerId, Class<T> attachmentClass) {
        Assert.notNull(ownerId, "attachment owner id is null");

        List<T> data = this.loadAttachments(ownerId, attachmentClass);

        if (data == null || data.isEmpty())
            return null;
        else
            return data.get(0);
    }

    public <T extends AbstractAttachment> List<T> loadAttachments(Serializable ownerId, Class<T> attachmentClass) {
        Assert.notNull(ownerId, "attachment owner id is null");

        @SuppressWarnings("unchecked")
        IAttachmentDao<T> dao = (IAttachmentDao<T>) this.getEntityDao(attachmentDaos, attachmentClass);

        return dao.loadByOwnerId(ownerId);
    }

}
