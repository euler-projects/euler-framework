/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2017 cFrost.sun(孙宾, SUN BIN) 
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
 * https://github.com/euler-form/web-form
 * https://cfrost.net
 */
package net.eulerframework.web.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * 未完成
 * @author cFrost
 * @deprecated
 */
public class ClassUtils {
    private final static ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    
    @SuppressWarnings("unchecked")
    public static <T> Set<Class<T>> findAllClassesImplementsInterface(Class<T> interfaceClass, String... scanPackages) throws IOException {
        Set<Class<?>> classes = findClasses(scanPackages);
        Set<Class<T>> result = new HashSet<>(); 
        for(Class<?> each : classes) {
            if(interfaceClass.isAssignableFrom(each)) {
                result.add((Class<T>)each);
            }
        }
        return result;
    }
    
    public static Set<Class<?>> findClasses(String... scanPackages) throws IOException {
        Resource[] resources = resourcePatternResolver.getResources("classpath*:/**/*.class");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader(); 
        String runtimePath = classLoader.getResource("").getPath();
        Set<Class<?>> classes = new HashSet<>(); 
        for(Resource resource : resources) {
            String packagePath;
            if("jar".equals(resource.getURI().getScheme())) {
                String uri = resource.getURI().toString();
                packagePath = uri.substring(uri.indexOf("jar!") + "jar!".length() + 1);
            } else if("file".equals(resource.getURI().getScheme())) {
                String uri = resource.getURI().toString();
                packagePath = uri.substring(("file:" + runtimePath).length());
            } else {
                continue;
            }
            String className = packagePath.substring(0, packagePath.length() - ".class".length()).replace('/', '.');
            
            if(scanPackages == null || scanPackages.length == 0) {
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (Throwable e) {
                    //DO_NOTHING
                }
            } else {                
                for(String scanPackage : scanPackages) {
                    if(className.startsWith(scanPackage)) {          
                        try {
                            Class<?> clazz = Class.forName(className);
                            classes.add(clazz);
                        } catch (Throwable e) {
                            //DO_NOTHING
                        }
                        break;
                    }
                }
                
            }
        }
        return classes;
    }

    public static <T> List<T> getEnumConstants(Class<T> enumClass, String... scanPackages) throws IOException {
        Set<Class<T>> classes = ClassUtils.findAllClassesImplementsInterface(enumClass, scanPackages);
        List<T> ret = new ArrayList<>();
        for(Class<T> clazz : classes) {
            if(clazz.isEnum()) {
                T[] constants = clazz.getEnumConstants();
                if(constants != null) {
                    ret.addAll(Arrays.asList(constants));
                }
            }
        }
        return ret;
    }

}
