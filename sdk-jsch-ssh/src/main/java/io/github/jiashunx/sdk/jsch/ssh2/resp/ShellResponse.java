package io.github.jiashunx.sdk.jsch.ssh2.resp;

import io.github.jiashunx.sdk.jsch.ssh2.exception.JschException;

/**
 * @author jiashunx
 */
public class ShellResponse {

    /**
     * 服务器IP.
     */
    private String host;

    /**
     * 异常对象.
     */
    private JschException errorObj;

    /**
     * ssh命令是否执行成功.
     */
    private boolean success = true;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public JschException getErrorObj() {
        return errorObj;
    }

    public void setErrorObj(JschException errorObj) {
        this.errorObj = errorObj;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
