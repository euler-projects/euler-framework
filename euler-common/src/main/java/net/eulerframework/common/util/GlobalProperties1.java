package net.eulerframework.common.util;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalProperties1 {
    
    protected final static Logger logger = LogManager.getLogger();
	
	private final static String CONFIG_FILE = "config.properties";

    private static PropertyReader reader;
    
    static{
        try {
            reader = new PropertyReader(CONFIG_FILE);
        } catch (IOException e) {
            throw new RuntimeException("配置文件classpath:"+CONFIG_FILE+"不存在",e);
        }
    }
    
    public static String get(String property) throws GlobalPropertyReadException {
        try {
        	String value = (String) reader.getProperty(property);
            logger.info("Load config: " + property + "=" + value);
            return value;
        } catch (NullValueException e) {
            throw new GlobalPropertyReadException(e);
        }
    }
    
    public static String get(String property, String defaultValue) {
        try {
            return get(property);
        } catch (GlobalPropertyReadException e) {
            logger.warn("Couldn't load "+ property +" , use " + defaultValue + " for default.");
            return defaultValue;
        }
    }
}
