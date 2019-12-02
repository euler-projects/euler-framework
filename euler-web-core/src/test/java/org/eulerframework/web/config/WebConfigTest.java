package org.eulerframework.web.config;

import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.Locale;

public class WebConfigTest {

    @Test
    public void getApplicationName() {
        Assert.assertEquals("TEST", WebConfig.getApplicationName());
        Assert.assertEquals(Locale.UK, WebConfig.getDefaultLanguage());
        Assert.assertArrayEquals(new Locale[] {Locale.CHINA, Locale.US, Locale.UK}, WebConfig.getSupportLanguages());
        Assert.assertEquals(Duration.ofSeconds(1), WebConfig.getRamCacheCleanFreq());
    }
}
