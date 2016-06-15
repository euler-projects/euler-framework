package net.eulerform.web.core.i18n;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;

public class ClassPathReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource implements ResourceLoaderAware {

    private static final String PROPERTIES_SUFFIX = ".properties";

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    
    @Override
    public void setBasenames(String... basenames) {
        if (basenames != null) {
            List<String> basenamesList = new ArrayList<>();
            for (int i = 0; i < basenames.length; i++) {
                String basename = basenames[i];
                Assert.hasText(basename, "Basename must not be empty");
                
                if(basename.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)){
                    Resource[] resources = null;
                    try {
                        resources = this.resourcePatternResolver.getResources(basename.trim() + PROPERTIES_SUFFIX);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    for(Resource resource : resources){
                        String path = null;
                        try {
                            path = resource.getURI().toString();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        String realFileName = path.substring(0, path.length()-PROPERTIES_SUFFIX.length());
                        basenamesList.add(realFileName);
                    }
                } else {
                    basenamesList.add(basename);
                }
            }
            super.setBasenames(basenamesList.toArray(new String[0]));
        }
    }
}
