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
