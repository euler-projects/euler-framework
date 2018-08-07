package net.eulerframework.web.core.exception.web;

/**
 * WEB异常代码
 * 
 * @author cFrost
 *
 */
public interface WebError {

    /**
     * Return the integer value of this web error code.
     */
    public int value();

    /**
     * Return the reason phrase of this web error code.
     */
    public String getReasonPhrase();
}