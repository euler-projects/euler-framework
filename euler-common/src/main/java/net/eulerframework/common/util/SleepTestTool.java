package net.eulerframework.common.util;

public class SleepTestTool {

    public static void sleep(int seconds) {
        int i = 0;
        while(i++ < seconds) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Sleep... "+ i +"s");
        }
    }
}
