package net.eulerframework.web.module.datastore.entity;

import java.util.Date;

public interface IArchivedObjectEntity {

    public Date getArchiveDate();
    public void setArchiveDate(Date archiveDate);
    
    public String getArchiveUserId();
    public void setArchiveUserId(String archiveUserId);
}
