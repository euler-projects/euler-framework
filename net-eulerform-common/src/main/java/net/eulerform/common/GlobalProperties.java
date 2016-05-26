package net.eulerform.common;

import java.io.IOException;

public class GlobalProperties {

    private static PropertyReader reader;
    
    static{
        final String configFileName = "config.properties";
        try {
            reader = new PropertyReader(configFileName);
        } catch (IOException e) {
            throw new RuntimeException("配置文件classpath:"+configFileName+"不存在",e);
        }
    }
    
    public static String get(String property) {
        try {
            return (String) reader.getProperty(property);
        } catch (NullValueException e) {
            throw new RuntimeException(e);
        }
    }
}
