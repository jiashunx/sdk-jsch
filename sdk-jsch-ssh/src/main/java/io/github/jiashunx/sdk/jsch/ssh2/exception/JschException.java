package io.github.jiashunx.sdk.jsch.ssh2.exception;

/**
 * @author jiashunx
 */
public class JschException extends RuntimeException {

    public JschException() {}

    public JschException(String message) {
        super(message);
    }

    public JschException(String message, Throwable cause) {
        super(message, cause);
    }

    public JschException(Throwable cause) {
        super(cause);
    }

}
