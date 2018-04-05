/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 cFrost.sun(孙宾, SUN BIN) 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following website
 * 
 * https://eulerproject.io
 * https://cfrost.net
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
