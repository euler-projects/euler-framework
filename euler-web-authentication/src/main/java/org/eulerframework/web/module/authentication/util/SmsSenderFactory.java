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
package org.eulerframework.web.module.authentication.util;

/**
 * 
 * 用于在发送短信前生成新的短信发送实现类实例，之所以每次生成新的实例是为了在出现并发时，每个实例的逻辑互不干扰。
 * 
 * @author cFrost
 *
 */
public interface SmsSenderFactory{
    
    abstract public SmsSender newSmsSender();
    
    public interface SmsSender {
        public void sendSms(String mobile, String msg);
    }
}
