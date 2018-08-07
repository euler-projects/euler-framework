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
package net.eulerframework.test;

import org.junit.Test;

/**
 * @author cFrost
 *
 */
public class RegexTest {

    @Test
    public void test() {
        //String regex = "^\\+?\\d+([-d]+)?$";
        String regex = "^[0-9\\+][0-9\\-]+$";
        System.out.println("+120-@3".matches(regex));
        System.out.println("+86 755 23440348".matches(regex));
        System.out.println("+86-183-0755-2123".matches(regex));
        System.out.println("18207552128".matches(regex));
        System.out.println("183-07552128".matches(regex));
        System.out.println("+183221230123-123".matches(regex));
        System.out.println("12923+123123".matches(regex));
    }
}
