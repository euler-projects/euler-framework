package net.eulerform.common;

import java.io.IOException;

public class GlobalProperties {
	
	private final static String CONFIG_FILE = "config.properties";

    private static PropertyReader reader;
    
    static{
        try {
            reader = new PropertyReader(CONFIG_FILE);
        } catch (IOException e) {
            throw new GlobalPropertyReadException("配置文件classpath:"+CONFIG_FILE+"不存在",e);
        }
    }
    
    public static String get(String property) {
        try {
            return (String) reader.getProperty(property);
        } catch (NullValueException e) {
            throw new GlobalPropertyReadException(e);
        }
    }
}
