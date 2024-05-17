package org.eulerframework.core.model;

import java.io.Serializable;
import java.util.Date;

public interface ResourceModel extends Serializable {
    String getTenantId();
    String getCreatedBy();
    String getLastModifiedBy();
    Date getCreatedDate();
    Date getLastModifiedDate();
}
