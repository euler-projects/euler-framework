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
import org.eulerframework.web.module.authentication.entity.EulerUserEntity;
import org.eulerframework.web.module.authentication.exception.UserNotFoundException;
import org.eulerframework.web.module.authentication.service.SmsCodeValidator.SmsCaptchaSenderFactory.SmsCaptchaSender;

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
    private SmsCaptchaSenderFactory smsCaptchaSenderFactory = new ConsoleSmsSenderFactory();
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private EulerUserEntityService eulerUserEntityService;

    public class SmsSendThread implements Runnable {
        private BizCode bizCode;
        private String mobile;
        private String captcha;
        private int expireMinutes;
        private SmsCaptchaSender smsCaptchaSender;

        public SmsSendThread(SmsCaptchaSender smsCaptchaSender, BizCode bizCode, String mobile, String captcha, int expireMinutes) {
            this.smsCaptchaSender = smsCaptchaSender;
            this.bizCode = bizCode;
            this.mobile = mobile;
            this.captcha = captcha;
            this.expireMinutes = expireMinutes;
        }

        @Override
        public void run() {
            this.smsCaptchaSender.sendSms(bizCode, mobile, captcha, expireMinutes);
        }

    }

    public void sendSmsCode(String mobile, BizCode bizCode) {
        Assert.hasText(mobile, "Required String parameter 'mobile' is not present");
        Assert.notNull(bizCode, "Required String parameter 'bizCode' is not present");

        String smsCode = this.generateSmsCode();
        String redisKey = this.generateRedisKey(mobile, bizCode);
        
        SmsCaptchaSender smsCaptchaSender = this.smsCaptchaSenderFactory.newSmsCaptchaSender();
        int expireMinutes;
        
        switch(bizCode) {
        case RESET_PASSWORD:
            try {
                EulerUserEntity userEntity = this.eulerUserEntityService.loadUserByMobile(mobile);
                if(!userEntity.isEnabled()) {
                    throw new SmsCodeNotSentException("_USER_IS_BLOCKED");
                }
            } catch (UserNotFoundException userNotFoundException) {
                throw new SmsCodeNotSentException("_MOBILE_NOT_EXISTS");
            }
            expireMinutes = SecurityConfig.getSmsCodeExpireMinutesResetPassword();
            break;
        case SIGN_IN:
            try {
                EulerUserEntity userEntity = this.eulerUserEntityService.loadUserByMobile(mobile);
                if(!userEntity.isEnabled()) {
                    throw new SmsCodeNotSentException("_USER_IS_BLOCKED");
                }
            } catch (UserNotFoundException userNotFoundException) {
                if(SecurityConfig.isEnableMobileAutoSignup()) {
                    this.logger.info("Mobile {} not exists but mobile auto sign up enabled, send the code for one key sign up.", mobile);
                } else {
                    throw new SmsCodeNotSentException("_MOBILE_NOT_EXISTS");
                }
            }
            expireMinutes = SecurityConfig.getSmsCodeExpireMinutesSignIn();
            break;
        case SIGN_UP:
            try {
                this.eulerUserEntityService.loadUserByMobile(mobile);
            } catch (UserNotFoundException userNotFoundException) {
                expireMinutes = SecurityConfig.getSmsCodeExpireMinutesSignUp();
                break;
            }
            throw new SmsCodeNotSentException("_MOBILE_ALREADY_BE_USED");
        default: return;
        }
        
        SmsSendThread thread = new SmsSendThread(smsCaptchaSender, bizCode, mobile, smsCode, expireMinutes);
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
        Assert.notNull(bizCode, "Required String parameter 'bizCode' is not present");

        String redisKey = this.generateRedisKey(mobile, bizCode);
        String realSmsCode = this.stringRedisTemplate.opsForValue().get(redisKey);

        if (StringUtils.hasText(realSmsCode) && realSmsCode.equalsIgnoreCase(smsCode)) {
            return;
        }
        
        throw new InvalidSmsCodeException();
    }
    
    public void check(String mobile, String smsCode, BizCode... bizCodes) throws InvalidSmsCodeException {
        if(!this.isEnabled()) {
            this.logger.info("Sms sender is disabled");
            return;
        }
        
        Assert.hasText(mobile, "Required String parameter 'mobile' is not present");
        Assert.hasText(smsCode, "Required String parameter 'smsCode' is not present");
        Assert.notEmpty(bizCodes, "Required String parameter 'bizCodes' is not present");

        for(BizCode bizCode : bizCodes) {
            String redisKey = this.generateRedisKey(mobile, bizCode);
            String realSmsCode = this.stringRedisTemplate.opsForValue().get(redisKey);

            if (StringUtils.hasText(realSmsCode) && realSmsCode.equalsIgnoreCase(smsCode)) {
                return;
            }
        }
        
        throw new InvalidSmsCodeException();
    }
    
    public boolean isEnabled() {
        return true;
        //TODO: 判定启用的逻辑不完备, 如果开发者的实现类也是不想启用短信验证码功能的类怎么处理?
        //return !(this.smsSenderFactory instanceof ConsoleSmsSenderFactory);
    }
    
    /**
     * 用于在发送短信前生成新的短信发送实现类实例，工厂实现类可自行决定是否采用单例模式。
     * 
     * @author cFrost
     *
     */
    public interface SmsCaptchaSenderFactory{
        
        abstract public SmsCaptchaSender newSmsCaptchaSender();
        
        public interface SmsCaptchaSender {
            public void sendSms(BizCode bizCode, String mobile, String captcha, int expireMinutes);
        }
    }
    
    public class ConsoleSmsSenderFactory implements SmsCaptchaSenderFactory {
        private SmsCaptchaSender sender = new ConsoleSmsCaptchaSender();

        @Override
        public SmsCaptchaSender newSmsCaptchaSender() {
            return sender;
        }
    }
    
    public class ConsoleSmsCaptchaSender implements SmsCaptchaSender {

        @Override
        public void sendSms(BizCode bizCode, String mobile, String captcha, int expireMinutes) {
            System.out.println(
                    String.format(
                            "Sms sender is disabled, bizCode: %s mobile: %s captcha: %s expireMinutes: %d", 
                            bizCode, mobile, captcha, expireMinutes));
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
