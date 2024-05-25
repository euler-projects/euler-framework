/*
 * Copyright 2013-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        Assert.assertEquals("default", WebConfig.getDefaultTheme());

        Assert.assertEquals(1024 * 1024 * 50L, WebConfig.getMultipartConfig().getMaxFileSize().toBytes());
    }
}
