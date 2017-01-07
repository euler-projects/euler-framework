package net.eulerframework.web.module.datastore.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@SuppressWarnings("serial")
@Entity
@Table(name = "ARCHIVE_FILE")
public class ArchivedFile extends UUIDEntity<ArchivedFile> implements IArchivedObjectEntity {

    @Column(name = "ORIGINAL_FILENAME", nullable = false)
    private String originalFilename;
    @Column(name = "ARCHIVED_PATH_SUFFIX")
    private String archivedPathSuffix;
    @Column(name = "ARCHIVED_FILENAME", nullable = false, unique = true)
    private String archivedFilename;
    @Column(name = "EXTENSION")
    private String extension;
    @Column(name = "MD5", nullable = false)
    private String md5;
    @Column(name = "FILE_BYTE_SIZE", nullable = false)
    private Long fileByteSize;
    @Column(name = "ARCHIVED_DATE", nullable = false)
    private Date archiveDate;
    @Column(name = "ARCHIVED_USER_ID", nullable = false)
    private String archiveUserId;

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getArchivedPathSuffix() {
        return archivedPathSuffix;
    }

    public void setArchivedPathSuffix(String archivedPathSuffix) {
        this.archivedPathSuffix = archivedPathSuffix;
    }

    public String getArchivedFilename() {
        return archivedFilename;
    }

    public void setArchivedFilename(String archivedFilename) {
        this.archivedFilename = archivedFilename;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getFileByteSize() {
        return fileByteSize;
    }

    public void setFileByteSize(Long fileByteSize) {
        this.fileByteSize = fileByteSize;
    }

    @Override
    public Date getArchiveDate() {
        return this.archiveDate;
    }

    @Override
    public void setArchiveDate(Date archiveDate) {
        this.archiveDate = archiveDate;

    }

    @Override
    public String getArchiveUserId() {
        return this.archiveUserId;
    }

    @Override
    public void setArchiveUserId(String archiveUserId) {
        this.archiveUserId = archiveUserId;
    }

}
