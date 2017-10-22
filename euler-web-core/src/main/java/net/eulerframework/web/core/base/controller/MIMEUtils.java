package net.eulerframework.web.core.base.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import net.eulerframework.common.util.StringUtils;
import net.eulerframework.common.util.property.PropertyReader;

public abstract class MIMEUtils {
    protected static final Logger LOGGER = LoggerFactory.getLogger(MIMEUtils.class);
    
    private static final String DEFAULT_CONFIG_VALUE = "application/octet-stream;attachment";
    private static final MIME DEFAULT_MIME = new MIME(DEFAULT_CONFIG_VALUE);

    private static final PropertyReader properties = new PropertyReader("/config-mime.properties");

    public static void reload() {
        properties.refresh();
    }
    
    public static MIME getDefaultMIME() {
        return DEFAULT_MIME;
    }

    public static MIME getMIME(String extension) {
        Assert.hasText(extension, "extension must not null");
        if(!extension.startsWith(".")) {
            extension = "." + extension;
        }
        return new MIME(properties.get(extension, DEFAULT_CONFIG_VALUE));
    }
    
    public static class MIME {
        private final static String SPLIT_CHAR = ";";
        
        private String contentType;
        private String contentDisposition;
        
        public MIME() {}
        
        public MIME(String configValue) {
            if(StringUtils.isEmpty(configValue) || configValue.indexOf(SPLIT_CHAR) < 0) {
                throw new InvalidPropertyValueException(configValue);
            }
            
            String[] configArray = configValue.split(SPLIT_CHAR);
            
            if(configArray.length < 2) {
                throw new InvalidPropertyValueException(configValue);
            }
            
            this.contentType = configArray[0];
            this.contentDisposition = configArray[1];
        }
        
        public String getContentType() {
            return contentType;
        }
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
        public String getContentDisposition() {
            return contentDisposition;
        }
        public void setContentDisposition(String contentDisposition) {
            this.contentDisposition = contentDisposition;
        }   
    }
}
