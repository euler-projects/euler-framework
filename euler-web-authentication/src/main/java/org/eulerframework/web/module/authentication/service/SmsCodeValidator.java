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
package org.eulerframework.web.module.authentication.service;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.eulerframework.common.base.log.LogSupport;
import org.eulerframework.web.core.exception.web.WebException;
import org.eulerframework.web.module.authentication.conf.SecurityConfig;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.util.SmsSenderFactory;
import org.eulerframework.web.module.authentication.util.SmsSenderFactory.SmsSender;

/**
 * @author cFrost
 *
 */
@Component
public class SmsCodeValidator extends LogSupport {
    private final static String REDIS_KEY_PERFIX = "euler_sms_code:";
    private final static Random RANDOM = new Random();
    private final static DecimalFormat DF = new DecimalFormat("0000");
    private ExecutorService threadPool = Executors.newFixedThreadPool(4);
    
    @Autowired(required = false)
    private SmsSenderFactory smsSenderFactory = new ConsoleSmsSenderFactory();
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private EulerUserEntityService eulerUserEntityService;

    public class SmsSendThread implements Runnable {
        private String mobile;
        private String msg;
        private SmsSender smsSender;

        public SmsSendThread(SmsSender smsSender, String mobile, String msg) {
            this.mobile = mobile;
            this.msg = msg;
            this.smsSender = smsSender;
        }

        @Override
        public void run() {
            this.smsSender.sendSms(mobile, msg);
        }

    }

    public void sendSmsCode(String mobile, BizCode bizCode) {
        Assert.hasText(mobile, "Required String parameter 'mobile' is not present");
        Assert.notNull(bizCode, "Required String parameter 'bizCode' is not present");

        String smsCode = this.generateSmsCode();
        String redisKey = this.generateRedisKey(mobile, bizCode);
        
        String msg;
        int expireMinutes;
        
        switch(bizCode) {
        case RESET_PASSWORD:
            try {
                this.eulerUserEntityService.loadUserByMobile(mobile);
            } catch (UserNotFoundException userNotFoundException) {
                throw new SmsCodeNotSentException("_MOBILE_NOT_EXISTS");
            }
            expireMinutes = SecurityConfig.getSmsCodeExpireMinutesResetPassword();
            msg = SecurityConfig.getSmsCodeTemplateResetPassword()
                    .replaceAll("\\$\\{sms_code\\}", smsCode)
                    .replaceAll("\\$\\{expire_minutes\\}", String.valueOf(expireMinutes));
            break;
        case SIGN_IN:
            try {
                this.eulerUserEntityService.loadUserByMobile(mobile);
            } catch (UserNotFoundException userNotFoundException) {
                throw new SmsCodeNotSentException("_MOBILE_NOT_EXISTS");
            }
            expireMinutes = SecurityConfig.getSmsCodeExpireMinutesSignIn();
            msg = SecurityConfig.getSmsCodeTemplateSignIn()
                    .replaceAll("\\$\\{sms_code\\}", smsCode)
                    .replaceAll("\\$\\{expire_minutes\\}", String.valueOf(expireMinutes));
            break;
        case SIGN_UP:
            try {
                this.eulerUserEntityService.loadUserByMobile(mobile);
            } catch (UserNotFoundException userNotFoundException) {
                expireMinutes = SecurityConfig.getSmsCodeExpireMinutesSignUp();
                msg = SecurityConfig.getSmsCodeTemplateSignUp()
                        .replaceAll("\\$\\{sms_code\\}", smsCode)
                        .replaceAll("\\$\\{expire_minutes\\}", String.valueOf(expireMinutes));
                break;
            }
            throw new SmsCodeNotSentException("_MOBILE_ALREADY_BE_USED");
        default: return;
        }
        
        SmsSender smsSender = this.smsSenderFactory.newSmsSender();
        SmsSendThread thread = new SmsSendThread(smsSender, mobile, msg);
        this.threadPool.submit(thread);

        this.stringRedisTemplate.opsForValue().set(redisKey, smsCode);
        this.stringRedisTemplate.expire(redisKey, expireMinutes, TimeUnit.MINUTES);
    }

    private String generateRedisKey(String mobile, BizCode bizCode) {
        return REDIS_KEY_PERFIX + bizCode.name().toLowerCase() + ":" + mobile;
    }

    private String generateSmsCode() {
        return DF.format(RANDOM.nextInt(9999));
    }

    public void check(String mobile, String smsCode, BizCode bizCode) throws InvalidSmsCodeException {
        if(!this.isEnabled()) {
            this.logger.info("Sms sender is disabled");
            return;
        }
        
        Assert.hasText(mobile, "Required String parameter 'mobile' is not present");
        Assert.hasText(smsCode, "Required String parameter 'smsCode' is not present");
        Assert.hasText(smsCode, "Required String parameter 'bizCode' is not present");

        String redisKey = this.generateRedisKey(mobile, bizCode);
        String realSmsCode = this.stringRedisTemplate.opsForValue().get(redisKey);

        if (StringUtils.hasText(realSmsCode) && realSmsCode.equalsIgnoreCase(smsCode)) {
            return;
        }
        
        throw new InvalidSmsCodeException();
    }
    
    public boolean isEnabled() {
        //TODO: 判定启用的逻辑不完备, 如果开发者的实现类也是不想启用短信验证码功能的类怎么处理?
        return !(this.smsSenderFactory instanceof ConsoleSmsSenderFactory);
    }
    
    public class ConsoleSmsSenderFactory implements SmsSenderFactory {
        private SmsSender sender = new ConsoleSmsSender();

        @Override
        public SmsSender newSmsSender() {
            return sender;
        }
        
    }
    
    public class ConsoleSmsSender implements SmsSender {

        @Override
        public void sendSms(String mobile, String msg) {
            System.out.println("Sms sender is disabled, mobile: " + mobile + " msg: " + msg);
        }
        
    }
    
    public enum BizCode {
        SIGN_UP, SIGN_IN, RESET_PASSWORD;
    }
    
    public class InvalidSmsCodeException extends WebException {
        public InvalidSmsCodeException() {
            super("_INVALID_SMS_CODE");
        }
    }
    
    public class SmsCodeNotSentException extends WebException {
        public SmsCodeNotSentException(String message) {
            super(message);
        }
    }

}