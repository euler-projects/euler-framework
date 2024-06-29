package org.eulerframework.security.core.captcha;

public enum CaptchaVisibility {
    /**
     * Only visible for the same request
     */
    REQUEST(0),
    /**
     * Only visible for the same session
     */
    SESSION(100),
    /**
     * Only visible to recipient
     */
    RECIPIENT(200),
    /**
     * Global visibility
     */
    GLOBAL(Integer.MAX_VALUE);

    private final int level;

    CaptchaVisibility(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
