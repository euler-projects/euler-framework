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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import javax.imageio.ImageIO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.util.StringUtils;

import org.eulerframework.web.core.exception.web.WebException;

/**
 * 验证码图片生成工具
 *  使用JAVA生成的图片验证码，调用getRandcode方法获取图片验证码，以流的方式传输到前端页面。
 *  
 * 
 * 作者: zhoubang 
 * 日期：2015年8月7日 上午10:41:05
 */
public class Captcha {

    private static final String RANDOMCODEKEY = "__euler_simple_captcha";// 放到session中的key
    private static final String RANDOMCODEADDTIME = "__euler_simple_captcha_add_time";// 放到session中的时刻
    private static final String REQUEST_PARAM_NAME = "captcha";// 默认用于传递验证码的参数
    private static final long LIFE_TIME = 10 * 60* 1000;// 验证码有效期
    
    private Random random = new Random();
    private String randString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            //+ "先帝创业未半而中道崩殂今天下三分益州疲弊此诚危急存亡之秋也然侍卫之臣不懈于内忠志之士忘身于外者盖追先帝之殊遇欲报之于陛下也诚宜开张圣听以光先帝遗德恢弘志士之气不宜妄自菲薄引喻失义以塞忠谏之路也宫中府中俱为一体陟罚臧否不宜异同若有作奸犯科及为忠善者宜付有司论其刑赏以昭陛下平明之理不宜偏私使内外异法也侍中侍郎郭攸之费祎董允等此皆良实志虑忠纯是以先帝简拔以遗陛下愚以为宫中之事事无大小悉以咨之然后施行必得裨补阙漏有所广益将军向宠性行淑均晓畅军事试用之于昔日先帝称之曰能是以众议举宠为督愚以为营中之事悉以咨之必能使行阵和睦优劣得所亲贤臣远小人此先汉所以兴隆也；亲小人远贤臣此后汉所以倾颓也先帝在时每与臣论此事未尝不叹息痛恨于桓灵也侍中尚书长史参军此悉贞良死节之臣愿陛下亲之信之则汉室之隆可计日而待也臣本布衣躬耕于南阳苟全性命于乱世不求闻达于诸侯先帝不以臣卑鄙猥自枉屈三顾臣于草庐之中咨臣以当世之事由是感激遂许先帝以驱驰后值倾覆受任于败军之际奉命于危难之间尔来二十有一年矣先帝知臣谨慎故临崩寄臣以大事也受命以来夙夜忧叹恐付托不效以伤先帝之明故五月渡泸深入不毛今南方已定兵甲已足当奖率三军北定中原庶竭驽钝攘除奸凶兴复汉室还于旧都此臣所以报先帝而忠陛下之职分也至于斟酌损益进尽忠言则攸之祎允之任也愿陛下托臣以讨贼兴复之效不效则治臣之罪以告先帝之灵若无兴德之言则责攸之祎允等之慢以彰其咎；陛下亦宜自谋以咨诹善道察纳雅言深追先帝遗诏臣不胜受恩感激今当远离临表涕零不知所言";// 随机产生的字符串

    private int width = 120;// 图片宽
    private int height = 34;// 图片高
    private int lineSize = 40;// 干扰线数量
    private int stringNum = 6;// 随机产生字符数量

    /*
     * 获得字体
     */
    private Font getFont() {
        return new Font("Fixedsys", Font.CENTER_BASELINE, 20);
    }

    /*
     * 获得颜色
     */
    private Color getRandColor(int fc, int bc) {
        if (fc > 255)
            fc = 255;
        if (bc > 255)
            bc = 255;
        int r = fc + random.nextInt(bc - fc - 16);
        int g = fc + random.nextInt(bc - fc - 14);
        int b = fc + random.nextInt(bc - fc - 18);
        return new Color(r, g, b);
    }

    /*
     * 绘制字符串
     */
    private String drowString(Graphics g, String randomString, int i) {
        g.setFont(getFont());
        g.setColor(new Color(random.nextInt(101), random.nextInt(111), random.nextInt(121)));
        String rand = String.valueOf(getRandomString(random.nextInt(randString.length())));
        randomString += rand;
        g.translate(random.nextInt(3), random.nextInt(6));
        g.drawString(rand, 16 * i, 16);
        return randomString;
    }

    /*
     * 绘制干扰线
     */
    private void drowLine(Graphics g) {
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int xl = random.nextInt(13);
        int yl = random.nextInt(15);
        g.drawLine(x, y, x + xl, y + yl);
    }

    /*
     * 获取随机的字符
     */
    public String getRandomString(int num) {
        return String.valueOf(randString.charAt(num));
    }
    
    
    /**
     * 生成随机图片
     * @throws IOException 
     */
    public void getRandcode(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        // BufferedImage类是具有缓冲区的Image类,Image类是用于描述图像信息的类
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics();// 产生Image对象的Graphics对象,改对象可以在图像上进行各种绘制操作
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.ROMAN_BASELINE, 18));
        g.setColor(getRandColor(110, 133));
        // 绘制干扰线
        for (int i = 0; i <= lineSize; i++) {
            drowLine(g);
        }
        // 绘制随机字符
        String randomString = "";
        for (int i = 1; i <= stringNum; i++) {
            randomString = drowString(g, randomString, i);
        }
        session.removeAttribute(RANDOMCODEKEY);
        session.removeAttribute(RANDOMCODEADDTIME);
        session.setAttribute(RANDOMCODEKEY, randomString);
        session.setAttribute(RANDOMCODEADDTIME, new Date());
        g.dispose();
        ImageIO.write(image, "JPEG", response.getOutputStream());// 将内存中的图片通过流动形式输出到客户端

    }
    
    public static String getRealCaptcha(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Object addTime = session.getAttribute(RANDOMCODEADDTIME);
        if(addTime == null || new Date().getTime() - ((Date) addTime).getTime() > LIFE_TIME) {
            return null;
        }
        return session.getAttribute(RANDOMCODEKEY).toString();
    }
    
    public static void validCaptcha(HttpServletRequest request) throws InvalidCaptchaException {
        validCaptcha(request.getParameter(REQUEST_PARAM_NAME), request);
    }

    /**
     * @param captcha
     * @param request
     * @throws InvalidCaptchaException 
     */
    public static void validCaptcha(String captcha, HttpServletRequest request) throws InvalidCaptchaException {
        String realCaptcha = Captcha.getRealCaptcha(request);
        if(StringUtils.hasText(realCaptcha) && realCaptcha.equalsIgnoreCase(captcha)) {
            
        } else {
            throw new InvalidCaptchaException();
        }
    }

    /**
     * @author cFrost
     *
     */
    public static class InvalidCaptchaException extends WebException {
        public InvalidCaptchaException() {
            super("_INVALID_CAPTCHA");
        }
    }
}
