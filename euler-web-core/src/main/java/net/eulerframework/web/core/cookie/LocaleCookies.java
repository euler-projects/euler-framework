package net.eulerframework.web.core.cookie;

/**
 * @author cFrost
 *
 */
public enum LocaleCookies implements EulerCookies {
    LOCALE("EULER_LOCALE", 10 * 365 * 24 * 60 * 60, "/");
    
    LocaleCookies(String name, int age, String path) {
        this.age = age;
        this.name = name;
        this.path = path;
    }
    
    private String name;
    private int age;
    private String path;

    @Override
    public String getCookieName() {
        // TODO Auto-generated method stub
        return name;
    }

    @Override
    public int getCookieAge() {
        // TODO Auto-generated method stub
        return age;
    }

    @Override
    public String getCookiePath() {
        // TODO Auto-generated method stub
        return path;
    }

}
