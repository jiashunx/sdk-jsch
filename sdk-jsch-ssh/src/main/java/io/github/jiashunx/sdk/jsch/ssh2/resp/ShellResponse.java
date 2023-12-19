package io.github.jiashunx.sdk.jsch.ssh2.resp;

import io.github.jiashunx.sdk.jsch.ssh2.exception.JschException;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author jiashunx
 */
public class ShellResponse {

    /**
     * 执行命令.
     */
    private String command;

    /**
     * 原始输出日志.
     */
    private String content;

    /**
     * 异常对象.
     */
    private JschException errorObj;

    /**
     * ssh命令是否执行成功.
     */
    private boolean success = true;

    /**
     * 已添加到输出队列
     */
    private boolean offeredToQueue = false;

    public String getCommand() {
        return command;
    }

    public ShellResponse setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ShellResponse setContent(String content) {
        this.content = content;
        return this;
    }

    public JschException getErrorObj() {
        return errorObj;
    }

    public ShellResponse setErrorObj(Throwable throwable) {
        return setErrorObj(throwable, false);
    }

    public ShellResponse setErrorObj(Throwable throwable, boolean append) {
        if (throwable instanceof JschException) {
            this.errorObj = (JschException) throwable;
        } else {
            this.errorObj = new JschException(throwable);
        }
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            if (append && getContent() != null) {
                stringWriter.append("\n------原输出内容------\n").append(getContent());
            }
            setContent(stringWriter.toString());
        }
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public ShellResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public boolean isOfferedToQueue() {
        return offeredToQueue;
    }

    public ShellResponse setOfferedToQueue(boolean offeredToQueue) {
        this.offeredToQueue = offeredToQueue;
        return this;
    }
}
