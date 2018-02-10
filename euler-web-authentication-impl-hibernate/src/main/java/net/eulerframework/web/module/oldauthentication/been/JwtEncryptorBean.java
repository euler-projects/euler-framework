package net.eulerframework.web.module.oldauthentication.been;

import java.io.File;
import java.io.FileNotFoundException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.eulerframework.common.base.log.LogSupport;
import net.eulerframework.common.util.io.file.FileReadException;
import net.eulerframework.common.util.io.file.SimpleFileIOUtils;
import net.eulerframework.common.util.jwt.JwtEncryptor;

@Configuration
public class JwtEncryptorBean extends LogSupport {

    @Bean(name = "jwtEncryptor")
    public JwtEncryptor jwtEncryptor() {
        JwtEncryptor j = new JwtEncryptor();
        j.setSigningKey(privKey());
        j.setVerifierKey(pubKey());
        return j;
    }

    public String privKey() {
        String path = this.getClass().getResource("/").getPath() + "rsa/resetPasswdPrivKey.pem";
        String key;
        try {
            key = new String(SimpleFileIOUtils.readFileByByte(new File(path)));
        } catch (FileNotFoundException | FileReadException e) {
            throw new RuntimeException(e);
        }
        this.logger.info("Load RSA private key file: " + path);
        return key;
    }

    public String pubKey() {
        String path = this.getClass().getResource("/").getPath() + "rsa/resetPasswdPubKey.pem";
        String key;
        try {
            key = new String(SimpleFileIOUtils.readFileByByte(new File(path)));
        } catch (FileNotFoundException | FileReadException e) {
            throw new RuntimeException(e);
        }
        this.logger.info("Load RSA public key file: " + path);
        return key;
    }

}
