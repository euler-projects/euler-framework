packages = {"**.web.**.controller"},
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
