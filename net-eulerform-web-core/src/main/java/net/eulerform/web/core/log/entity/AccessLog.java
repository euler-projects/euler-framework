package net.eulerform.web.core.log.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import net.eulerform.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name="SYS_ACCESS_LOG")
public class AccessLog extends UUIDEntity<AccessLog> {

    @Column(name="CLIENT_IP")
    private String clientIp;
    @Column(name="URI")
    private String uri;
    @Column(name="ACCESS_DATE")
    private Date accessDate;
    public String getClientIp() {
        return clientIp;
    }
    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
    public String getUri() {
        return uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public Date getAccessDate() {
        return accessDate;
    }
    public void setAccessDate(Date accessDate) {
        this.accessDate = accessDate;
    }
}
