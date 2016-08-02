

import java.io.IOException;
import java.util.Map;

import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerform.web.core.base.entity.WebServiceResponse;

public class WebServiceResponseRestTemplate extends OAuth2RestTemplate {
    
    public WebServiceResponseRestTemplate(OAuth2ProtectedResourceDetails resource) {
        super(resource);
    }
    
    public WebServiceResponseRestTemplate(OAuth2ProtectedResourceDetails resource, OAuth2ClientContext context) {
        super(resource, context);
    }
    
    private ObjectMapper objectMapper;
 
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // GET
    public <T> WebServiceResponse<T> getForWebServiceResponse(String url, TypeReference<?> valueTypeRef, Object... urlVariables) throws IOException {
        
        @SuppressWarnings("unchecked")
        WebServiceResponse<T> webServiceResponse = this.getForObject(url, WebServiceResponse.class, urlVariables);
        String json = this.objectMapper.writeValueAsString(webServiceResponse);
        webServiceResponse= this.objectMapper.readValue(json, valueTypeRef);
        return webServiceResponse;
    }
    public <T> WebServiceResponse<T> getForWebServiceResponse(String url, TypeReference<?> valueTypeRef, Map<String, ?> urlVariables) throws IOException {
        
        @SuppressWarnings("unchecked")
        WebServiceResponse<T> webServiceResponse = this.getForObject(url, WebServiceResponse.class, urlVariables);
        String json = this.objectMapper.writeValueAsString(webServiceResponse);
        webServiceResponse= this.objectMapper.readValue(json, valueTypeRef);
        return webServiceResponse;
    }
    public <T> WebServiceResponse<T> getForWebServiceResponse(String url, TypeReference<?> valueTypeRef) throws IOException {
        
        @SuppressWarnings("unchecked")
        WebServiceResponse<T> webServiceResponse = this.getForObject(url, WebServiceResponse.class);
        String json = this.objectMapper.writeValueAsString(webServiceResponse);
        webServiceResponse= this.objectMapper.readValue(json, valueTypeRef);
        return webServiceResponse;
    }
    
    //POST
    public <T> WebServiceResponse<T> postForWebServiceResponse(String url, Object request, TypeReference<?> valueTypeRef, Object... urlVariables) throws IOException {
        
        @SuppressWarnings("unchecked")
        WebServiceResponse<T> webServiceResponse = this.postForObject(url, request, WebServiceResponse.class, urlVariables);
        String json = this.objectMapper.writeValueAsString(webServiceResponse);
        webServiceResponse= this.objectMapper.readValue(json, valueTypeRef);
        return webServiceResponse;
    }
    public <T> WebServiceResponse<T> postForWebServiceResponse(String url, Object request, TypeReference<?> valueTypeRef, Map<String, ?> urlVariables) throws IOException {
        
        @SuppressWarnings("unchecked")
        WebServiceResponse<T> webServiceResponse = this.postForObject(url, request, WebServiceResponse.class, urlVariables);
        String json = this.objectMapper.writeValueAsString(webServiceResponse);
        webServiceResponse= this.objectMapper.readValue(json, valueTypeRef);
        return webServiceResponse;
    }
    public <T> WebServiceResponse<T> postForWebServiceResponse(String url, Object request, TypeReference<?> valueTypeRef) throws IOException {
        
        @SuppressWarnings("unchecked")
        WebServiceResponse<T> webServiceResponse = this.postForObject(url, request, WebServiceResponse.class);
        String json = this.objectMapper.writeValueAsString(webServiceResponse);
        webServiceResponse= this.objectMapper.readValue(json, valueTypeRef);
        return webServiceResponse;
    }
}
