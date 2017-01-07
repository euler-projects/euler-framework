package net.eulerframework.common.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class StringTool {
    private static final String REGEX_MULTISPACE = "[^\\S\\r\\n]+";// 除换行外的连续空白字符
    private static final String REGEX_HTNL_SCRIPT = "<script[^>]*?>[\\s\\S]*?<\\/script>"; // 定义script的正则表达式
    private static final String REGEX_HTML_STYPE = "<style[^>]*?>[\\s\\S]*?<\\/style>"; // 定义style的正则表达式
    private static final String REGEX_HTML_TAG = "<[^>]+>"; // 定义HTML标签的正则表达式
    // private static final String regEx_space = "\\s*|\t";//定义空格制表符符
    // private static final String regEx_space = "\\s*|\t|\r|\n";//定义空格回车换行符

    public final static boolean isNull(String inputStr) {
        return inputStr == null || inputStr.trim().equals("") || inputStr.trim().toLowerCase().equals("null");
    }

    public final static int getStringBytesLength(String string) {
        if (isNull(string))
            return 0;

        return string.getBytes().length;
    }

    /**
     * 按字节长度截取字符串
     *
     * @param string
     *            要截取的字符串
     * @param subBytes
     *            截取字节长度
     * @param suffix
     *            如果发生截取,在结果后添加的后缀,为<code>null</code>表示不添加
     * @return 截取后字符串
     */
    public static String subStringByBytes(String string, int subBytes, String suffix) {
        byte[] stringBytes = string.getBytes();
        if (stringBytes.length <= subBytes)
            return string;

        byte[] subStringBytes = Arrays.copyOf(stringBytes, subBytes);
        String subString;
        try {
            subString = new String(subStringBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        subString = subString.substring(0, subString.length() - 1);

        if (!StringTool.isNull(suffix)) {
            subString += suffix;
        }

        return subString;
    }

    /**
     * 将制表符和多个连续的空格用一个空格替代
     *
     * @param string 待处理的字符串
     * @return 处理后的字符串
     */
    public static String earseMultiSpcases(String string) {
        if (string == null)
            return string;

        Pattern p_space = Pattern.compile(REGEX_MULTISPACE, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(string);
        string = m_space.replaceAll(" "); // 过滤空格制表符标签
        return string.trim(); // 返回文本字符串
    }

    /**
     * 删除制表符和空格
     *
     * @param string 待处理的字符串
     * @return 处理后的字符串
     */
    public static String earseAllSpcases(String string) {
        if (string == null)
            return string;

        return earseMultiSpcases(string).replace(" ", "");
    }

    /**
     * 将CRLF和CR换行符转换为LF换行符
     *
     * @param string 待处理的字符串
     * @return 处理后的字符串
     */
    public static String convertToLF(String string) {
        if (string == null)
            return string;
        return string.replace("\r\n", "\n").replace("\r", "\n");
    }

    /**
     * 将换行符替换为空格
     *
     * @param string 待处理的字符串
     * @return 处理后的字符串
     */
    public static String convertReturnToSpace(String string) {
        if (string == null)
            return string;
        return convertToLF(string).replace("\n", " ");
    }

    /**
     * 删除Html标签
     *
     * @param htmlStr 带有HTML标记的字符串
     * @return 清除标记后的字符串
     */
    public static String earseHTMLTag(String htmlStr) {

        if (htmlStr == null)
            return htmlStr;

        Pattern p_script = Pattern.compile(REGEX_HTNL_SCRIPT, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); // 过滤script标签

        Pattern p_style = Pattern.compile(REGEX_HTML_STYPE, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); // 过滤style标签

        Pattern p_html = Pattern.compile(REGEX_HTML_TAG, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); // 过滤html标签
        htmlStr.replaceAll("&nbsp;", " ");// 替换空格
        return htmlStr.trim(); // 返回文本字符串
    }

    /**
     * 删除换行符
     *
     * @param string 待处理的字符串
     * @return 处理后的字符串
     */
    public static String earseReturn(String string) {

        if (string == null)
            return string;

        return string.replace("\r\n", "").replace("\r", "").replace("\n", "");
    }

    /**
     * 首字母转小写
     * @param string 待转换的字符串
     * @return 转换后的字符串
     */
    public static String toLowerCaseFirstChar(String string) {
        if(string == null || string.length() == 0)
            return string;

        if (Character.isLowerCase(string.charAt(0)))
            return string;

        if(string.length() == 1)
            return string.toLowerCase();
        else
            return (new StringBuilder()).append(Character.toLowerCase(string.charAt(0))).append(string.substring(1)).toString();
    }

    /**
     * 首字母转大写
     * @param string 待转换的字符串
     * @return 转换后的字符串
     */
    public static String toUpperCaseFirstChar(String string) {
        if(string == null || string.length() == 0)
            return string;

        if (Character.isUpperCase(string.charAt(0)))
            return string;

        if(string.length() == 1)
            return string.toUpperCase();
        else
            return (new StringBuilder()).append(Character.toUpperCase(string.charAt(0))).append(string.substring(1)).toString();
    }

    /**
     * 随机生成字符串,字符串可能的取值在ASCII 0x21-0x7e之间
     * @param length 生成的字符串长度
     * @return
     */
    public static String randomString(int length) {
        StringBuffer stringBuffer = new StringBuffer();
        
        Random random = new Random();
        for(; length > 0; length--) {
            stringBuffer.append((char)(random.nextInt(93)+33));
        }
        return stringBuffer.toString();
    }
    
    public static void main(String[] args) {
        Integer i = 100;
        System.out.println(randomString(i));
        System.out.println(i);
    }
}
