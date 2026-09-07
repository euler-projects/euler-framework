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

import org.junit.Assert;
import org.junit.Test;

public class TemplateTextUtilsTest {

    private final TemplateTextUtils text = TemplateTextUtils.INSTANCE;

    @Test
    public void latinNameInAChineseSentenceIsSetOffBySpaces() {
        Assert.assertEquals("使用 Google 登录", text.autospace("使用Google登录"));
    }

    @Test
    public void chineseNameInAChineseSentenceIsLeftTight() {
        Assert.assertEquals("使用邮箱登录", text.autospace("使用邮箱登录"));
        Assert.assertEquals("使用密码登录", text.autospace("使用密码登录"));
    }

    @Test
    public void anEnglishSentenceIsUntouched() {
        Assert.assertEquals("Sign in with Google", text.autospace("Sign in with Google"));
        Assert.assertEquals("Sign in with Password", text.autospace("Sign in with Password"));
    }

    @Test
    public void digitsCountAsLatin() {
        Assert.assertEquals("剩余 30 秒", text.autospace("剩余30秒"));
    }

    @Test
    public void aMixedNameIsSpacedAtEveryBoundaryItCrosses() {
        // The Latin run inside the name meets ideographs on both sides, so
        // both boundaries take a space - there is nothing special about a
        // boundary that happens to fall inside the substituted name.
        Assert.assertEquals("使用 Google 账号登录", text.autospace("使用Google账号登录"));
    }

    @Test
    public void existingSpacingIsKept() {
        // Only adjacency matches, so a bundle that already spaced the
        // pattern by hand is not doubled up on.
        Assert.assertEquals("使用 Google 登录", text.autospace("使用 Google 登录"));
    }

    @Test
    public void theOperationIsIdempotent() {
        String once = text.autospace("使用Google登录，剩余30秒");
        Assert.assertEquals(once, text.autospace(once));
        Assert.assertEquals("使用 Google 登录，剩余 30 秒", once);
    }

    @Test
    public void punctuationBetweenScriptsIsNotSeparated() {
        // The comma already separates the scripts optically; inserting a
        // space around it would look like a typo.
        Assert.assertEquals("邮箱：a@b.c", text.autospace("邮箱：a@b.c"));
    }

    @Test
    public void japaneseAndKoreanAreTreatedAsIdeographic() {
        Assert.assertEquals("ログイン Google で", text.autospace("ログインGoogleで"));
        Assert.assertEquals("로그인 Google 으로", text.autospace("로그인Google으로"));
    }

    @Test
    public void degenerateInputIsReturnedAsIs() {
        Assert.assertNull(text.autospace(null));
        Assert.assertEquals("", text.autospace(""));
        Assert.assertEquals("使", text.autospace("使"));
    }
}
