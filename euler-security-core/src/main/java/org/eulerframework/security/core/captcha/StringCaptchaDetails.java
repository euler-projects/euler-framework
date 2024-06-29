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
package org.eulerframework.security.core.captcha;

import org.springframework.util.Assert;

import java.util.*;

public class StringCaptchaDetails implements CaptchaDetails {
    private final CaptchaVisibility visibility;
    private final String[] scopes;
    private final String captcha;
    private final boolean caseSensitive;

    private StringCaptchaDetails(String captcha, CaptchaVisibility visibility, boolean caseSensitive, String... scopes) {
        Assert.hasText(captcha, "captcha is empty");
        Assert.notNull(visibility, "visibility is null");
        Assert.notNull(scopes, "scopes is null");
        this.captcha = captcha;
        this.visibility = visibility;
        this.caseSensitive = caseSensitive;
        this.scopes = scopes;
    }

    @Override
    public CaptchaVisibility getVisibility() {
        return visibility;
    }

    @Override
    public String[] getScopes() {
        return scopes;
    }

    @Override
    public String getCaptcha() {
        return captcha;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public static StringCaptchaBuilder builder() {
        return new StringCaptchaBuilder();
    }

    public static final class StringCaptchaBuilder {
        private String captcha;

        private final Set<String> scopes = new HashSet<>();

        private CaptchaVisibility visibility;

        private boolean caseSensitive = false;

        /**
         * Creates a new instance
         */
        private StringCaptchaBuilder() {
        }

        public StringCaptchaBuilder captcha(String captcha) {
            this.captcha = captcha;
            return this;
        }

        public StringCaptchaBuilder scope(String... scopes) {
            this.scopes.addAll(Arrays.asList(scopes));
            return this;
        }

        public StringCaptchaBuilder visibility(CaptchaVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public StringCaptchaBuilder caseSensitive(boolean caseSensitive) {
            this.caseSensitive = caseSensitive;
            return this;
        }

        public StringCaptchaDetails build() {
            Assert.hasText(this.captcha, "captcha is empty");
            Assert.notEmpty(this.scopes, "scopes is empty");
            Assert.notNull(this.visibility, "visibility is null");
            return new StringCaptchaDetails(this.captcha, this.visibility, this.caseSensitive, this.scopes.toArray(new String[0]));
        }
    }
}
