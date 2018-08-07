/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.eulerframework.web.core.extend;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * 支持使用通配符扫描AliasesPackage的SqlSessionFactoryBean
 * @author cFrost
 *
 */
public class AliasesPackageMatchingSqlSessionFactoryBean extends SqlSessionFactoryBean {
    
    private final static String JAR = "jar!";

    /**
     * 设置AliasesPackage扫描包，可设置多个，支持通配符
     * @param typeAliasesPackages eg. net.**.web.**.entity or net.demo.web.entity
     */
    public void setTypeAliasesPackages(String[] typeAliasesPackages) {
        Assert.notEmpty(typeAliasesPackages, "typeAliasesPackages can not be empty");
        
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        
        Set<Resource> typeAliasesPackageResources = new HashSet<>();
        for(String typeAliasesPackage : typeAliasesPackages) {
            String locationPattern = "classpath*:" + typeAliasesPackage.trim().replace(".", "/");

            try {
                typeAliasesPackageResources.addAll(Arrays.asList(resolver.getResources(locationPattern)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        Set<String> resourcePath = new HashSet<>();
        String classPath = this.getClass().getResource("/").toString();
        
        for(Resource resource : typeAliasesPackageResources) {
            if (!StringUtils.hasText(resource.getDescription())) {
                continue;
            }
            
            try {
                String uri = resource.getURI().toString();
                String path = uri.substring(classPath.length() - 1);
                
                if(path.indexOf(JAR) > -1) {
                    path = path.substring(path.indexOf(JAR) + JAR.length());
                }
                
                while (path.startsWith("/")) {
                    path = path.substring(1);
                }
                
                if (!StringUtils.hasText(path)) {
                    continue;
                }
                
                resourcePath.add(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        StringBuffer stringBuffer = new StringBuffer();
        
        for(String each : resourcePath) {
            while (each.endsWith("/")) {
                each = each.substring(0, each.length() - 1);
            }
            each= each.replace('/', '.');
            stringBuffer.append(each).append(',');
        }
        
        super.setTypeAliasesPackage(stringBuffer.toString());
    }
}
