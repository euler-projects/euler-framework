package org.eulerframework.data.file.web.security;

public interface FileTokenRegistry {
    FileToken generateToken(String fileId);

    FileToken getTokenByTokenValue(String tokenValue);
}
