package org.eulerframework.security.jackson2;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eulerframework.security.core.userdetails.EulerUserDetails;
import org.springframework.security.jackson2.CoreJackson2Module;

import java.util.ArrayList;
import java.util.List;

public class EulerSecurityJackson2ModuleTest {
    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new CoreJackson2Module());
        mapper.registerModule(new EulerSecurityJackson2Module());
        List<Object> rawList = new ArrayList<>();
        rawList.add(new EulerUserDetails("1", "1", "1", new ArrayList<>()));
        String json = mapper.writeValueAsString(rawList);
        System.out.println(json);
        List<Object> readList = mapper.readValue(json, List.class);
        System.out.println(mapper.writeValueAsString(readList));
    }
}