package net.eulerframework.web.core.base.response;

/**
 * @author cFrost
 *
 */
public class CmdResponse implements BaseResponse {
    
    public enum Cmd {
        REDIRECT, SIGN_OUT;
    }

    private Cmd cmd;
    
    public CmdResponse(Cmd cmd) {
        this.cmd = cmd;
    }

    public Cmd getCmd() {
        return cmd;
    }

    public void setCmd(Cmd cmd) {
        this.cmd = cmd;
    }
    
}
