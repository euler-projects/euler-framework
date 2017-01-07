package net.eulerframework.web.core.base.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import net.eulerframework.common.util.FileReader;
import net.eulerframework.web.core.base.WebContextAccessable;
import net.eulerframework.web.core.base.response.HttpStatusResponse;
import net.eulerframework.web.core.base.response.Status;
import net.eulerframework.web.core.exception.BadRequestException;
import net.eulerframework.web.core.exception.IllegalParamException;
import net.eulerframework.web.core.exception.ResourceExistException;
import net.eulerframework.web.core.exception.ResourceNotFoundException;

public abstract class BaseController extends WebContextAccessable {
    
    protected final Logger logger = LogManager.getLogger(this.getClass());
    
    protected void writeString(String string) throws IOException{
        this.getResponse().getOutputStream().write(string.getBytes("UTF-8"));
    }

    protected void writeFile(String fileName, File file) throws FileNotFoundException, IOException {
        byte[] fileData = FileReader.readFileByMultiBytes(file, 1024);
        
        HttpServletResponse response = this.getResponse();

        response.setCharacterEncoding("utf-8");
        response.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        response.setHeader("Content-Disposition", 
                "attachment;fileName=" + new String(fileName.getBytes("utf-8"), "ISO8859-1"));

        response.setStatus(HttpStatus.OK.value());
        response.getOutputStream().write(fileData);
    }
    
    protected void setNoCacheHeader() {
        HttpServletResponse response = this.getResponse();
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Date", new Date().getTime());
        response.setIntHeader("Expires", 0);
    }
    
    /**  
     * 用于在程序发生{@link BadRequestException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class})   
    public Object badRequestException(BadRequestException e) {
        this.logger.error(e.getMessage(), e);
        return new HttpStatusResponse(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
    }
    
    /**  
     * 用于在程序发生{@link ResourceExistException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ResourceExistException.class})   
    public Object exception(ResourceExistException e) {
        e.printStackTrace();
        return new HttpStatusResponse(Status.RESOURCE_EXIST, e.getLocalizedMessage());
    }
    
    /**  
     * 用于在程序发生{@link IllegalParamException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalParamException.class})   
    public Object illegalParamException(IllegalParamException e) {
        this.logger.error(e.getMessage(), e);
        return new HttpStatusResponse(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
    }
    
    /**  
     * 用于在程序发生{@link IllegalArgumentException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})   
    public Object illegalArgumentException(IllegalArgumentException e) {
        this.logger.error(e.getMessage(), e);
        return new HttpStatusResponse(HttpStatus.BAD_REQUEST, e.getLocalizedMessage());
    }
    
    /**  
     * 用于在程序发生{@link ResourceNotFoundException}异常时统一返回错误信息
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ResourceNotFoundException.class})   
    public Object resourceNotFoundException(ResourceNotFoundException e) {
        e.printStackTrace();
        return new HttpStatusResponse(HttpStatus.NOT_FOUND);
    }
    
    /**  
     * 用于在程序发生{@link AccessDeniedException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class})   
    public Object accessDeniedException(AccessDeniedException e) {
        return new HttpStatusResponse(HttpStatus.FORBIDDEN);
    }
    
    /**  
     * 用于在程序发生{@link BindException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BindException.class})   
    public Object bindException(BindException e) {
        e.printStackTrace();
        List<ObjectError> errors = e.getAllErrors();
        List<String> errMsg = new ArrayList<>();
        for(ObjectError err : errors){
            if(FieldError.class.isAssignableFrom(err.getClass()))
                errMsg.add(((FieldError)err).getField()+ ": " + err.getDefaultMessage());
            else
                errMsg.add(err.getDefaultMessage());
        }
        return new HttpStatusResponse(Status.FIELD_VALID_FAILED, errMsg.toString());
    }
    
    /**  
     * 用于在程序发生{@link MissingServletRequestParameterException}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MissingServletRequestParameterException.class})   
    public Object missingServletRequestParameterException(MissingServletRequestParameterException e) {
        e.printStackTrace();
        return  new HttpStatusResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }
    
    /**  
     * 用于在程序发生{@link Exception}异常时统一返回错误信息 
     * @return  
     */  
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({Exception.class})   
    public Object exception(Exception e) {
        e.printStackTrace();
        return new HttpStatusResponse(HttpStatus.INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
    }
}
