package net.eulerframework.web.module.authentication.bean;

import org.springframework.context.annotation.Configuration;

import net.eulerframework.common.base.log.LogSupport;

@Configuration
public class JwtEncryptorBean extends LogSupport {

//    @Bean(name = "jwtEncryptor")
//    public JwtEncryptor jwtEncryptor() {
//        JwtEncryptor j = new JwtEncryptor();
//        j.setSigningKey(privKey());
//        j.setVerifierKey(pubKey());
//        return j;
//    }
//
//    public String privKey() {
//        String path = this.getClass().getResource("/").getPath() + "rsa/resetPasswdPrivKey.pem";
//        String key;
//        try {
//            key = new String(SimpleFileIOUtils.readFileByByte(new File(path)));
//        } catch (FileNotFoundException | FileReadException e) {
//            throw new RuntimeException(e);
//        }
//        this.logger.info("Load RSA private key file: " + path);
//        return key;
//    }
//
//    public String pubKey() {
//        String path = this.getClass().getResource("/").getPath() + "rsa/resetPasswdPubKey.pem";
//        String key;
//        try {
//            key = new String(SimpleFileIOUtils.readFileByByte(new File(path)));
//        } catch (FileNotFoundException | FileReadException e) {
//            throw new RuntimeException(e);
//        }
//        this.logger.info("Load RSA public key file: " + path);
//        return key;
//    }

}
