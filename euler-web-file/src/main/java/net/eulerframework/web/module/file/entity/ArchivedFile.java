package net.eulerframework.web.module.file.entity;

import java.io.File;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

import net.eulerframework.web.core.base.entity.UUIDEntity;

@Entity
@Table(name = "basic_uploaded_file")
public class ArchivedFile extends UUIDEntity<ArchivedFile> {

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;
    @Column(name = "archived_path_suffix")
    private String archivedPathSuffix;
    @Column(name = "archived_filename", nullable = false, unique = true)
    private String archivedFilename;
    @Column(name = "extension")
    private String extension;
    @Column(name = "md5", nullable = false)
    private String md5;
    @Column(name = "file_byte_size", nullable = false)
    private Long fileByteSize;
    @Column(name = "uploaded_date", nullable = false)
    private Date uploadedDate;
    @Column(name = "uploaded_user_id", nullable = false)
    private String uploadedUserId;
    
    @Transient
    private File archivedFile;

    public File getArchivedFile() {
        return archivedFile;
    }

    public void setArchivedFile(File archivedFile) {
        this.archivedFile = archivedFile;
    }

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

    public Date getUploadedDate() {
        return this.uploadedDate;
    }

    public void setUploadedDate(Date archiveDate) {
        this.uploadedDate = archiveDate;

    }

    public String getUploadedUserId() {
        return this.uploadedUserId;
    }

    public void setUploadedUserId(String archiveUserId) {
        this.uploadedUserId = archiveUserId;
    }

}
