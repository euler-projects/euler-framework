package net.eulerframework.common.util;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalProperties1 {
    
    protected final static Logger logger = LogManager.getLogger();
	
	private final static String CONFIG_FILE = "config.properties";

    private static PropertyReader reader;
    
    static{
        refresh();
    }

    public static void refresh() {
        try {
            reader = new PropertyReader(CONFIG_FILE);
            logger.info("Refresh File Config");
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

    public static int getIntValue(String property, int defaultValue) {
        try {
            return Integer.parseInt(get(property));
        } catch (GlobalPropertyReadException e) {
            logger.warn("Couldn't load "+ property +" , use " + defaultValue + " for default.");
            return defaultValue;
        }
    }
    
    public static long getLongValue(String property, long defaultValue) {
        try {
            return Long.parseLong(get(property));
        } catch (GlobalPropertyReadException e) {
            logger.warn("Couldn't load "+ property +" , use " + defaultValue + " for default.");
            return defaultValue;
        }
    }
    

    public static double getDoubleValue(String property, double defaultValue) {
        try {
            return Double.parseDouble(get(property));
        } catch (GlobalPropertyReadException e) {
            logger.warn("Couldn't load "+ property +" , use " + defaultValue + " for default.");
            return defaultValue;
        }
    }

    public static boolean getBooleanValue(String property, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(get(property));
        } catch (GlobalPropertyReadException e) {
            logger.warn("Couldn't load "+ property +" , use " + defaultValue + " for default.");
            return defaultValue;
        }
    }
    
    /**
     * 读取枚举类型的配置
     * @param property 参数名
     * @param defaultValue 默认值，在读不到的时候返回此值
     * @param toUpperCase 是否将读取到的字符串转为大写后再转为对应的Enum
     * @return 配置了正确的参数按配置返回，未配置或配置参数不正确返回默认值
     */
    public static <T extends Enum<T>> T getEnumValue(String property, T defaultValue, boolean toUpperCase) {
        try {
            String configValue = get(property);
            
            if(toUpperCase)
                configValue = configValue.toUpperCase();
            
            return T.valueOf(defaultValue.getDeclaringClass(), configValue);
        } catch (GlobalPropertyReadException e) {
            logger.warn("Couldn't load "+ property +" , use " + defaultValue + " for default.");
            return defaultValue;
        } catch (IllegalArgumentException e) {
            logger.warn(property +" was configed as a wrong value , use " + defaultValue + " for default.");
            return defaultValue;
        }
    }
}
