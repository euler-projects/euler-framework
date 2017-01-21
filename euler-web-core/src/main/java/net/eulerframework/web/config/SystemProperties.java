package net.eulerframework.web.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class SystemProperties {

    private final static String path = "/system.properties";
    private final static Properties props;
    
    static {
        InputStream inputStream = SystemProperties.class.getResourceAsStream(path);
            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream));
            props = new Properties();        
            try {
                props.load(bufferedReader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
    
    public static String frameworkVersion() {
        return props.getProperty("version");
    }
}
