/*
 * The MIT License (MIT)
 * 
 * Copyright (c) 2013-2018 Euler Project
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * For more information, please visit the following websites
 * 
 * https://eulerproject.io/euler-framework
 */
package net.eulerframework.web.module.authentication.service;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import net.eulerframework.web.module.authentication.exception.NotSupportRobotCheckRequestException;
import net.eulerframework.web.module.authentication.util.SmsSenderFactory;
import net.eulerframework.web.module.authentication.util.SmsSenderFactory.SmsSender;

/**
 * @author cFrost
 *
 */
public class MobileCodeRobotCheckService implements RobotCheckService {
    private final static String REDIS_KEY_PERFIX = "EULER_SMS_CODE:";
    private final static Random RANDOM = new Random();
    private final static DecimalFormat DF = new DecimalFormat("0000");
    private SmsSenderFactory smsSenderFactory;
    private StringRedisTemplate stringRedisTemplate;
    private ExecutorService threadPool = Executors.newFixedThreadPool(4);

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

    public SmsSenderFactory getSmsSenderFactory() {
        return smsSenderFactory;
    }

    public void setSmsSenderFactory(SmsSenderFactory smsSenderFactory) {
        this.smsSenderFactory = smsSenderFactory;
    }

    public StringRedisTemplate getStringRedisTemplate() {
        return stringRedisTemplate;
    }

    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void sendSmsCode(HttpServletRequest request) {
        String mobile = request.getParameter("mobile");
        Assert.hasText(mobile, "Required String parameter 'mobile' is not present");
        String redisKey = generateRedisKey(mobile);
        String smsCode = this.generateSmsCode();

        SmsSender smsSender = this.smsSenderFactory.newSmsSender();
        SmsSendThread thread = new SmsSendThread(smsSender, mobile, smsCode);
        this.threadPool.submit(thread);

        this.stringRedisTemplate.opsForValue().set(redisKey, smsCode);
        this.stringRedisTemplate.expire(redisKey, 10, TimeUnit.MINUTES);
    }

    private String generateRedisKey(String mobile) {
        return REDIS_KEY_PERFIX + mobile;
    }

    private String generateSmsCode() {
        return DF.format(RANDOM.nextInt(9999));
    }

    @Override
    public boolean isRobot(HttpServletRequest request) throws NotSupportRobotCheckRequestException{
        String mobile = request.getParameter("mobile");
        String smsCode = request.getParameter("smsCode");
//        Assert.hasText(mobile, "Required String parameter 'mobile' is not present");
//        Assert.hasText(smsCode, "Required String parameter 'smsCode' is not present");
        
        if(StringUtils.isEmpty(mobile) || StringUtils.isEmpty(smsCode)) {
            throw new NotSupportRobotCheckRequestException();
        }

        String redisKey = generateRedisKey(mobile);
        String realSmsCode = this.stringRedisTemplate.opsForValue().get(redisKey);

        if (StringUtils.hasText(realSmsCode) && realSmsCode.equalsIgnoreCase(smsCode)) {
            return false;
        }
        return true;
    }

}
