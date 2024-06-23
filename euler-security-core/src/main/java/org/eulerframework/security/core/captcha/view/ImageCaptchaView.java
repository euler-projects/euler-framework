package org.eulerframework.security.core.captcha.view;

import org.eulerframework.security.core.captcha.StringCaptcha;

import java.io.IOException;
import java.io.OutputStream;

public interface ImageCaptchaView {
    void draw(StringCaptcha captcha, String formatName, OutputStream out) throws IOException;
}
