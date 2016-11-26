package net.eulerframework.web.module.ueditor.service;

import javax.servlet.http.HttpServletRequest;

import net.eulerframework.web.core.exception.MultipartFileSaveException;
import net.eulerframework.web.module.ueditor.entity.FileUploadResult;
import net.eulerframework.web.module.ueditor.entity.UeConfig;
import org.springframework.web.multipart.MultipartFile;

import net.eulerframework.web.core.base.service.IBaseService;

public interface IUeService extends IBaseService {
    
    public UeConfig config(HttpServletRequest request);
    
    public FileUploadResult uploadImg(HttpServletRequest request, MultipartFile file) throws MultipartFileSaveException;

    public FileUploadResult uploadFile(HttpServletRequest request, MultipartFile file) throws MultipartFileSaveException;

}
