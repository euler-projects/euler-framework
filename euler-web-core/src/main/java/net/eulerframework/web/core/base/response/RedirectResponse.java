package net.eulerframework.web.core.base.response;

/**
 * @author cFrost
 *
 */
public class RedirectResponse extends CmdResponse {

    public RedirectResponse(String location) {
        super(Cmd.REDIRECT);
        this.location = location;
    }

    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
