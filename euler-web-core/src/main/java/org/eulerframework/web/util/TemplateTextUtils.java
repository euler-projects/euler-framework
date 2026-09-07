/*
 * Copyright 2013-present the original author or authors.
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
package org.eulerframework.web.util;

import java.util.regex.Pattern;

/**
 * Text helpers for templates, published to them as {@code euler.text}.
 *
 * <p>Stateless and thread-safe.
 */
public final class TemplateTextUtils {

    /**
     * The instance templates are handed. Its methods are the published
     * surface; the class carries no state.
     */
    public static final TemplateTextUtils INSTANCE = new TemplateTextUtils();

    private static final String IDEOGRAPH =
            "\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}\\p{IsHangul}";
    private static final String ALPHANUMERIC = "A-Za-z0-9";

    private static final Pattern IDEOGRAPH_THEN_ALPHANUMERIC =
            Pattern.compile("(?<=[" + IDEOGRAPH + "])(?=[" + ALPHANUMERIC + "])");
    private static final Pattern ALPHANUMERIC_THEN_IDEOGRAPH =
            Pattern.compile("(?<=[" + ALPHANUMERIC + "])(?=[" + IDEOGRAPH + "])");

    private TemplateTextUtils() {
    }

    /**
     * Separates ideographic script from adjacent Latin letters and digits
     * with a space, the convention Chinese, Japanese and Korean typography
     * follows for mixed-script text.
     *
     * <p>This exists because the boundary belongs to neither party. A
     * message bundle cannot place the space: whether one is needed depends
     * on the value substituted into the pattern, and a Chinese page names
     * an identity provider {@code Google} as readily as it names a channel
     * {@code 邮箱}. Nor can a stylesheet: the CSS that would do it,
     * {@code text-autospace}, is unsupported in Chromium. So each bundle
     * writes its own language plainly - {@code 使用{0}登录} in Chinese,
     * {@code Sign in with {0}} in English - and the boundary is settled
     * here, once, for whatever the substitution turns out to be.
     *
     * <p>Only adjacency is matched, so text that already carries a space
     * is returned unchanged and the operation is idempotent. Text with no
     * ideographs - every Latin-script locale - is likewise untouched.
     *
     * @param text the text to space out; may be {@code null}
     * @return the spaced text, or {@code text} itself when there is
     *         nothing to separate
     */
    public String autospace(String text) {
        if (text == null || text.length() < 2) {
            return text;
        }
        String spaced = IDEOGRAPH_THEN_ALPHANUMERIC.matcher(text).replaceAll(" ");
        return ALPHANUMERIC_THEN_IDEOGRAPH.matcher(spaced).replaceAll(" ");
    }
}
