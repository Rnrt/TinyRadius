package org.tinyradius.yijiupi;

public class AuthResult {
    private boolean success;
    private String reason;

    public AuthResult(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    public AuthResult(boolean success) {
        this(success, null);
    }

    public static AuthResult create(boolean success, String reason) {
        return new AuthResult(success, reason);
    }

    public static AuthResult success() {
        return new AuthResult(true);
    }

    public static AuthResult fail(String reason) {
        return new AuthResult(false, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
