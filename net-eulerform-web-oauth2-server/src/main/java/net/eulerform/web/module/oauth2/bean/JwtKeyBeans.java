package net.eulerform.web.module.oauth2.bean;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import net.eulerform.common.FileReader;

@Configuration
@Profile({"oauth-authorization-server","oauth-resource-server"})
public class JwtKeyBeans {

	private KeyPair keyPair;
	private String jwtVerifierKey;
	private String jwtSigningKey;

	@Value("${oauth.jwtSigningKeyFile}")
	private String jwtSigningKeyFile;
	@Value("${oauth.jwtVerifierKeyFile}")
	private String jwtVerifierKeyFile;

	@Bean(name = "keyPair")
	public KeyPair rsaKeys() throws NoSuchAlgorithmException {
		if (this.keyPair != null)
			return this.keyPair;
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
		keyPairGen.initialize(2048);
		this.keyPair = keyPairGen.generateKeyPair();
		return this.keyPair;
	}

	@Bean(name = "jwtVerifierKey")
	public String jwtVerifierKey() throws IOException {
		if (this.jwtVerifierKey != null)
			return this.jwtVerifierKey;
		String path = this.getClass().getResource("/").getPath();
		this.jwtVerifierKey = FileReader.readFileByLines(path + jwtVerifierKeyFile);
		return this.jwtVerifierKey;
	}

	@Bean(name = "jwtSigningKey")
	public String jwtSigningKey() throws IOException {
		if (this.jwtSigningKey != null)
			return this.jwtSigningKey;
		String path = this.getClass().getResource("/").getPath();
		this.jwtSigningKey = FileReader.readFileByLines(path + jwtSigningKeyFile);
		return this.jwtSigningKey;
	}

}
