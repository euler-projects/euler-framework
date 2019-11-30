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
package org.eulerframework.web.module.authentication.bean;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.common.util.io.file.FileReadException;
import org.eulerframework.common.util.io.file.SimpleFileIOUtils;
import org.eulerframework.common.util.jwt.JwtEncryptor;
import org.eulerframework.web.module.authentication.conf.SecurityConfigExternal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileNotFoundException;

@Configuration
public class JwtEncryptorBean extends LogSupport {

    @Bean
    public JwtEncryptor jwtEncryptor() {
        JwtEncryptor j = new JwtEncryptor();
        j.setSigningKey(privKey());
        j.setVerifierKey(pubKey());
        return j;
    }

    public String privKey() {
        String path = SecurityConfigExternal.getResetPasswordPrivKeyFile();
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
        String path = SecurityConfigExternal.getResetPasswordPubKeyFile();
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
