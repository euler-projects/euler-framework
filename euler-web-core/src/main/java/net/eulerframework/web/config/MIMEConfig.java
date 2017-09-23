package net.eulerframework.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import net.eulerframework.cache.inMemoryCache.DefaultObjectCache;
import net.eulerframework.cache.inMemoryCache.ObjectCachePool;
import net.eulerframework.common.util.property.PropertyReader;

@Configuration
public abstract class MIMEConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(MIMEConfig.class);
    
    private static final String DEFAULT_CONFIG_VALUE = "application/octet-stream;attachment";
    private static final MIME DEFAULT_MIME = new MIME(DEFAULT_CONFIG_VALUE);

    private static final DefaultObjectCache<String, MIME> CONFIG_CAHCE = ObjectCachePool
            .generateDefaultObjectCache(Long.MAX_VALUE);

    private static final PropertyReader properties = new PropertyReader("/config-mime.properties");

    public static boolean clearWebConfigCache() {
        properties.refresh();
        return CONFIG_CAHCE.clear();
    }
    
    public static MIME getDefaultMIME() {
        return DEFAULT_MIME;
    }

    public static MIME getMIME(String extension) {
        Assert.hasText(extension, "extension must not null");
        if(!extension.startsWith(".")) {
            extension = "." + extension;
        }
        return CONFIG_CAHCE.get(extension, key -> new MIME(properties.get(key, DEFAULT_CONFIG_VALUE)));
    }
}
