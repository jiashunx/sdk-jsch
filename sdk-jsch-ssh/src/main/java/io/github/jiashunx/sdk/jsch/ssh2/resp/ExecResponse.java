package io.github.jiashunx.sdk.jsch.ssh2.resp;

import io.github.jiashunx.sdk.jsch.ssh.SSHConst;
import io.github.jiashunx.sdk.jsch.ssh2.exception.JschException;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author jiashunx
 */
public class ExecResponse {

    /**
     * 服务器IP.
     */
    private String host;

    /**
     * 执行命令.
     */
    private String command;

    /**
     * 命令执行耗时.
     */
    private long commandCostMillis;

    /**
     * 原始输出日志.
     */
    private String outputContent;

    /**
     * 异常输出日志.
     */
    private String errorContent;

    /**
     * 异常对象.
     */
    private JschException errorObj;

    /**
     * ssh命令是否执行成功.
     */
    private boolean success;

    /**
     * 获取控制台输出内容行记录.
     * @return String[]
     */
    public String[] getOutputContentLines() {
        String[] lines = null;
        if (outputContent != null) {
            lines = outputContent.split(SSHConst.STRING_LINE_SEPARATOR);
        }
        if (lines == null || lines.length == 0) {
            lines = new String[] { SSHConst.STRING_EMPTY };
        }
        return lines;
    }

    /**
     * 获取错误输出内容行记录.
     * @return String[]
     */
    public String[] getErrorContentLines() {
        String[] lines = null;
        if (errorContent != null) {
            lines = errorContent.split(SSHConst.STRING_LINE_SEPARATOR);
        }
        if (lines == null || lines.length == 0) {
            lines = new String[] { SSHConst.STRING_EMPTY };
        }
        return lines;
    }

    public String getHost() {
        return host;
    }

    public ExecResponse setHost(String host) {
        this.host = host;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public ExecResponse setCommand(String command) {
        this.command = command;
        return this;
    }

    public long getCommandCostMillis() {
        return commandCostMillis;
    }

    public ExecResponse setCommandCostMillis(long commandCostMillis) {
        this.commandCostMillis = commandCostMillis;
        return this;
    }

    public String getOutputContent() {
        if (outputContent == null) {
            return SSHConst.STRING_EMPTY;
        }
        return outputContent;
    }

    public ExecResponse setOutputContent(String outputContent) {
        this.outputContent = outputContent;
        return this;
    }

    public String getErrorContent() {
        if (errorContent == null) {
            return SSHConst.STRING_EMPTY;
        }
        return errorContent;
    }

    public ExecResponse setErrorContent(String errorContent) {
        this.errorContent = errorContent;
        return this;
    }

    public ExecResponse setErrorContent(Throwable throwable) {
        this.errorObj = new JschException(throwable);
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            setErrorContent(stringWriter.toString());
        }
        return this;
    }

    public JschException getErrorObj() {
        return errorObj;
    }

    public boolean isSuccess() {
        return success;
    }

    public ExecResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

}
