package net.eulerform.web.core.security.config;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import net.eulerform.common.ReadFromFile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class Config {

    @Bean(name="keyPair")
    public KeyPair rsaKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA"); 
        keyPairGen.initialize(2048); 
        KeyPair keyPair = keyPairGen.generateKeyPair(); 
        return keyPair;
    }    

    @Value("${jwtPrivateKeyFilePath}")
    private String jwtPrivateKeyFile;
    @Value("${jwtPublicKeyFilePath}")
    private String jwtPublicKeyFile;
    
    @Bean(name="jwtTokenVerifierKey")
    public String jwtTokenVerifierKey() throws IOException{
        String path = this.getClass().getResource("/").getPath();
        String jwtTokenVerifierKey = ReadFromFile.readFileByLines(path+jwtPublicKeyFile);
        return jwtTokenVerifierKey;
    }
    
    @Bean(name="jwtTokenSigningKey")
    public String jwtTokenSigningKey() throws IOException{
        String path = this.getClass().getResource("/").getPath();
        String jwtTokenSigningKey = ReadFromFile.readFileByLines(path+jwtPrivateKeyFile);
        return jwtTokenSigningKey;
    }

}
