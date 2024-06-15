/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eulerframework.context.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class ClassPathReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource
        implements ResourceLoaderAware {

    private static final String PROPERTIES_SUFFIX = ".properties";
    private static final String CLASS_PATH_PREFIX = "WEB-INF/classes/";
    private static final String JAR_PATH_PREFIX = "jar:";
    private static final String FILE_PATH_PREFIX = "file:";

    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    @Override
    public void setBasename(@NonNull String basename) {
        this.setBasenames(basename);
    }

    @Override
    public void setBasenames(@NonNull String... basenames) {
        if (!ArrayUtils.isEmpty(basenames)) {
            List<String> basenamesList = new ArrayList<>();
            for (String basename : basenames) {
                Assert.hasText(basename, "Basename must not be empty");

                if (basename.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)) {
                    Resource[] resources = null;
                    try {
                        resources = this.resourcePatternResolver.getResources(basename.trim() + PROPERTIES_SUFFIX);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    for (Resource resource : resources) {
                        String path = null;
                        try {
                            path = resource.getURL().toString();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        String realFileName = path.substring(0, path.length() - PROPERTIES_SUFFIX.length());

                        if (this.isClassPathFile(realFileName)) {
                            String relativePath = this.generateClassPathBasename(realFileName);
                            if (!basenamesList.contains(relativePath)) {
                                basenamesList.add(relativePath);
                            }
                        } else if (this.isJarPathFile(realFileName)) {
                            String relativePath = this.generateJarPathBasename(realFileName);
                            if (!basenamesList.contains(relativePath)) {
                                basenamesList.add(relativePath);
                            }
                        } else if (this.isFilePathFile(realFileName)) {
                            String relativePath = this.generateFilePathBasename(realFileName);
                            if (!basenamesList.contains(relativePath)) {
                                basenamesList.add(relativePath);
                            }
                        }
                    }
                } else {
                    basenamesList.add(basename);
                }
            }

            super.setBasenames(basenamesList.toArray(new String[0]));
        }
    }

    private String generateFilePathBasename(String realFileName) {
        String relativePath = realFileName;
        if (relativePath.indexOf('_') >= 0) {
            relativePath = relativePath.substring(0, relativePath.indexOf('_'));
        }
        return relativePath;
    }

    private String generateJarPathBasename(String realFileName) {
        String relativePath = realFileName;
        if (relativePath.indexOf('_') >= 0) {
            relativePath = relativePath.substring(0, relativePath.indexOf('_'));
        }
        return relativePath;
    }

    private boolean isJarPathFile(String realFileName) {
        return realFileName.startsWith(JAR_PATH_PREFIX);
    }

    private String generateClassPathBasename(String realFileName) {

        String relativePath = realFileName.substring(realFileName.lastIndexOf(CLASS_PATH_PREFIX));

        if (relativePath.indexOf('_') >= 0) {
            relativePath = relativePath.substring(0, relativePath.indexOf('_'));
        }

        return relativePath;
    }

    private boolean isClassPathFile(String realFileName) {
        return realFileName.lastIndexOf(CLASS_PATH_PREFIX) >= 0;
    }

    private boolean isFilePathFile(String realFileName) {
        return realFileName.startsWith(FILE_PATH_PREFIX);
    }
}
