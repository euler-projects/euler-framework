package net.eulerform.web.core.base.entity;

public enum WebResponseStatus {
    
    RESOURCE_EXIST(-100, "Resource Existed"),
    
    FIELD_VALID_FAILED(-101, "Field valid failed");

    private final int value;

    private final String reasonPhrase;


    private WebResponseStatus(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    /**
     * Return the integer value of this status code.
     */
    public int value() {
        return this.value;
    }

    /**
     * Return the reason phrase of this status code.
     */
    public String getReasonPhrase() {
        return reasonPhrase;
    }
}
