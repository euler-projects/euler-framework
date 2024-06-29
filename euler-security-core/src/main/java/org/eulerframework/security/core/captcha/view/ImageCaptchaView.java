package org.eulerframework.security.core.captcha.view;

import org.eulerframework.security.core.captcha.StringCaptchaDetails;

import java.io.IOException;
import java.io.OutputStream;

public interface ImageCaptchaView {
    void draw(StringCaptchaDetails captcha, String formatName, OutputStream out) throws IOException;
}
