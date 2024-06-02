package org.eulerframework.security.core.captcha.util;

import org.eulerframework.security.core.captcha.StringCaptcha;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class ImageStringCaptchaDrawer {
    private final Random random = new Random();

    private int width = 120;// 图片宽
    private int height = 34;// 图片高
    private int lineSize = 40;// 干扰线数量

    public void drawCaptchaImage(OutputStream out, StringCaptcha stringCaptcha) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics();
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.BOLD, 18));
        g.setColor(getRandColor(110, 133));
        // 绘制干扰线
        for (int i = 0; i <= lineSize; i++) {
            drawLine(g);
        }
        // 绘制随机字符
        drawString(g, stringCaptcha.getCaptcha());
        g.dispose();
        ImageIO.write(image, "JPEG", out);
    }

    /*
     * 获得字体
     */
    private Font getFont() {
        return new Font("Fixedsys", Font.BOLD, 20);
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
    private void drawString(Graphics g, String string) {
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            g.setFont(getFont());
            g.setColor(new Color(random.nextInt(101), random.nextInt(111), random.nextInt(121)));
            g.translate(random.nextInt(3), random.nextInt(6));
            g.drawChars(chars, i, 1, 16 * i, 16);
        }
    }

    /*
     * 绘制干扰线
     */
    private void drawLine(Graphics g) {
        int x = random.nextInt(width);
        int y = random.nextInt(height);
        int xl = random.nextInt(13);
        int yl = random.nextInt(15);
        g.drawLine(x, y, x + xl, y + yl);
    }
}
