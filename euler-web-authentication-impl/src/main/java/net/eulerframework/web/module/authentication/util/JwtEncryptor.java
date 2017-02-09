package net.eulerframework.web.module.authentication.util;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.jwt.crypto.sign.SignatureVerifier;
import org.springframework.security.jwt.crypto.sign.Signer;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.eulerframework.web.module.authentication.exception.InvalidJwtException;

public class JwtEncryptor {

    protected final Logger logger = LogManager.getLogger(this.getClass());
    
    private String signingKey;
    private String verifierKey;
    private Signer signer;
    private SignatureVerifier verifier;
    private ObjectMapper objectMapper;
    
    public JwtEncryptor() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.setSerializationInclusion(Include.NON_NULL);
    }
    
    public void setSigningKey(String key) {
        Assert.hasText(key);
        key = key.trim();

        this.signingKey = key;

        if (isPublic(this.signingKey)) {
            signer = new RsaSigner(this.signingKey);
            logger.info("Configured with RSA signing key");
        }
        else {
            throw new IllegalArgumentException(
                    "SigningKey property must be a RSA private key");
        }
    }
    
    /**
     * @return true if the key has a public verifier
     */
    private boolean isPublic(String key) {
        return key.startsWith("-----BEGIN");
    }
    
    public void setVerifierKey(String key) {
        this.verifierKey = key;
        try {
            new RsaSigner(verifierKey);
            throw new IllegalArgumentException(
                    "Private key cannot be set as verifierKey property");
        }
        catch (Exception expected) {
            // Expected
        }
        
        this.verifier = new RsaVerifier(verifierKey);
    }
    
    public Jwt encode (Object obj) {
        try {
            return JwtHelper.encode(this.objectMapper.writeValueAsString(obj), signer);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert object to JSON");
        }
    }
    
    private Jwt decodeOnly(String jwtStr) throws InvalidJwtException {
        try {
            return JwtHelper.decode(jwtStr);
        } catch (Exception e) {
            throw new InvalidJwtException("Cannot decode jwt string", e);
        }
    }
    
    /**
     * 解码token
     * @param jwtStr token
     * @return 解码后的Jwt对象
     * @throws InvalidJwtException 验证不通过
     */
    public Jwt decode(String jwtStr) throws InvalidJwtException {
        try {
            Jwt jwt = this.decodeOnly(jwtStr);

            jwt.verifySignature(verifier);
            
            return jwt;
        } catch (InvalidJwtException invalidJwtException) {
            throw invalidJwtException;
        } catch (Exception e) {
            throw new InvalidJwtException("Invalid jwt string", e);
        }
    }
    
    /**
     * 将token中的claims转为对象
     * @param jwtStr token
     * @param claimsType claims对应的对象类型
     * @return claims对象
     * @throws InvalidJwtException 验证不通过
     * @throws IOException 对象Json解析错误
     */
    public <T> T decodeClaims(String jwtStr, Class<T> claimsType) throws InvalidJwtException, IOException {
        Jwt jwt = this.decode(jwtStr);
        return this.objectMapper.readValue(jwt.getClaims(), claimsType);
    }
}
