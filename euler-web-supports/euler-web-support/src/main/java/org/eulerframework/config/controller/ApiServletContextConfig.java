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
package org.eulerframework.config.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Resource;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.eulerframework.web.core.annotation.ApiEndpoint;

@Configuration
@EnableWebMvc
@ComponentScan(
        basePackages = {"**.web.**.controller"},
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(ApiEndpoint.class),
        excludeFilters = @ComponentScan.Filter(
                type=FilterType.ASPECTJ, 
                pattern={
                        "*..web..controller.admin..*"
                        })
)
@ImportResource({"classpath*:config/controller-security.xml"})
public class ApiServletContextConfig implements WebMvcConfigurer {
    
    @Resource(name="objectMapper") ObjectMapper objectMapper;
//    @Resource(name="jaxb2Marshaller") Marshaller marshaller;
//    @Resource(name="jaxb2Marshaller") Unmarshaller unmarshaller;

    @Override
    public void configureMessageConverters(
            List<HttpMessageConverter<?>> converters
    ) {
        converters.add(new SourceHttpMessageConverter<>());

//        MarshallingHttpMessageConverter xmlConverter =
//                new MarshallingHttpMessageConverter();
//        xmlConverter.setSupportedMediaTypes(Arrays.asList(
//                MediaType.APPLICATION_XML,
//                MediaType.TEXT_XML
//        ));
//        xmlConverter.setMarshaller(this.marshaller);
//        xmlConverter.setUnmarshaller(this.unmarshaller);
//        converters.add(xmlConverter);

        MappingJackson2HttpMessageConverter jsonConverter =
                new MappingJackson2HttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(Arrays.asList(
                MediaType.APPLICATION_JSON_UTF8,
                MediaType.valueOf("text/json;charset=UTF-8")
        ));
        jsonConverter.setObjectMapper(this.objectMapper);
        converters.add(jsonConverter);
        
    }
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }

    @Override
    public void configureContentNegotiation(
            ContentNegotiationConfigurer configurer)
    {
        Map<String, MediaType> mediaTypes = new HashMap<>();;
        mediaTypes.put("json", MediaType.APPLICATION_JSON_UTF8);
        //mediaTypes.put("xml", MediaType.APPLICATION_XML);
        
        configurer.favorPathExtension(false).favorParameter(false)
                .ignoreAcceptHeader(false)
                .defaultContentType(MediaType.APPLICATION_JSON_UTF8)
                .mediaTypes(mediaTypes);
    }
}
