package net.eulerframework.web.config;

import net.eulerframework.common.util.property.PropertyNotFoundException;
import net.eulerframework.common.util.property.PropertyReader;

/**
 * 用户获取系统参数
 * @author cFrost
 *
 */
public final class SystemProperties {

    private final static PropertyReader properties = new PropertyReader("/system.properties");
    
    public static String frameworkVersion() {
        try {
            return properties.get("version");
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
