package net.eulerframework.web.core.util;

import java.util.Properties;


public class Config {
    
    protected static Properties config;
    
    public static Properties getConfig() {
        return config;
    }

    public static void setConfig(Properties config) {
        Config.config = config;
    }

    public static String getConfigStr(String key) {
        return config.getProperty(key);
    }
}
