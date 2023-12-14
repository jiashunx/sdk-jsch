package io.github.jiashunx.sdk.jsch.ssh;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author jiashunx
 */
public class SSHResponse {

    /**
     * 远程服务器IP.
     */
    private String remoteHost;
    /**
     * 执行命令.
     */
    private String command;
    /**
     * 原始输出日志.
     */
    private String outputContent;
    /**
     * 异常输出日志.
     */
    private String errorContent;
    /**
     * ssh命令是否成功.
     */
    private boolean success;
    /**
     * 命令执行耗时
     */
    private long commandCostMillis;

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

    public String getRemoteHost() {
        return remoteHost;
    }
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    public String getCommand() {
        return command;
    }
    public void setCommand(String command) {
        this.command = command;
    }
    public String getOutputContent() {
        if (outputContent == null) {
            return SSHConst.STRING_EMPTY;
        }
        return outputContent;
    }
    public void setOutputContent(String outputContent) {
        this.outputContent = outputContent;
    }
    public String getErrorContent() {
        if (errorContent == null) {
            return SSHConst.STRING_EMPTY;
        }
        return errorContent;
    }
    public void setErrorContent(String errorContent) {
        this.errorContent = errorContent;
    }
    public void setErrorContent(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            setErrorContent(stringWriter.toString());
        }
    }
    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public long getCommandCostMillis() {
        return commandCostMillis;
    }
    public void setCommandCostMillis(long commandCostMillis) {
        this.commandCostMillis = commandCostMillis;
    }

}
