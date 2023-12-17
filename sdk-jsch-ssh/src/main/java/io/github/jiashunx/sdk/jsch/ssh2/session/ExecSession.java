package io.github.jiashunx.sdk.jsch.ssh2.session;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.Session;
import io.github.jiashunx.sdk.jsch.ssh.SSHConst;
import io.github.jiashunx.sdk.jsch.ssh2.resp.ExecResponse;
import io.github.jiashunx.sdk.jsch.ssh2.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiashunx
 */
public class ExecSession extends AbstractSession {

    private static final Logger logger = LoggerFactory.getLogger(ExecSession.class);

    /**
     * 执行命令后是否立即关闭session
     * true-执行命令后立即关闭，下次执行时重新连接
     * false-执行命令后不关闭session
     */
    private boolean autoClose = false;

    public ExecSession(Server server) {
        super(server);
    }

    @Override
    public void close() {
        getServer().release();
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public ExecSession setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
        return this;
    }

    public ExecResponse exec(String command) {
        return execMulti(command).get(0);
    }

    public List<ExecResponse> execMulti(String... commands) {
        return splitResponses(execute(commands), commands);
    }

    private synchronized ExecResponse execute(String... commands) {
        String[] commandArr = commands;
        if (commandArr == null || commandArr.length == 0) {
            commandArr = new String[] { SSHConst.STRING_EMPTY };
        }
        // 多命令处理，插入标记命令，作为同一命令进行执行
        // 对ssh命令进行格式化, 统一进行命令分隔处理（主要针对多命令执行）
        StringBuilder commandBuilder = new StringBuilder();
        for (String c: commandArr) {
            commandBuilder.append(c).append(SSHConst.STRING_SEMICOLON)
                    .append(SSHConst.COMMAND_ECHO_STDOUT_LINE).append(SSHConst.STRING_SEMICOLON)
                    .append(SSHConst.COMMAND_ECHO_STDERR_LINE).append(SSHConst.STRING_SEMICOLON);
        }
        return execute(commandBuilder.toString());
    }

    private synchronized ExecResponse execute(String command) {
        Server server = getServer();
        ExecResponse execResponse = new ExecResponse();
        execResponse.setCommand(command);
        execResponse.setHost(server.getHost());
        Session session = null;
        ChannelExec channelExec = null;
        long startMillis = System.currentTimeMillis();
        long costMillis = 0L;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errStream = new ByteArrayOutputStream()) {
            // 获取session，自动重连
            session = server.getSession();
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            channelExec.setInputStream(null);
            channelExec.setErrStream(errStream);
            channelExec.setOutputStream(outputStream);
            channelExec.connect(server.getChannelConnectTimeoutMillis());
            while (!channelExec.isClosed()) {
                Thread.sleep(10L);
            }
            execResponse.setSuccess(true);
            execResponse.setErrorContent(errStream.toString());
            execResponse.setOutputContent(outputStream.toString());
            costMillis = System.currentTimeMillis() - startMillis;
        } catch (Throwable e) {
            execResponse.setSuccess(false);
            execResponse.setErrorContent(e);
            costMillis = System.currentTimeMillis() - startMillis;
        } finally {
            if (channelExec != null) {
                channelExec.disconnect();
            }
            if (isAutoClose()) {
                close();
            }
            execResponse.setCommandCostMillis(costMillis);
        }
        return execResponse;
    }

    private List<ExecResponse> splitResponses(ExecResponse response, String... commands) {
        String[] commandArr = commands;
        if (commandArr == null || commandArr.length == 0) {
            commandArr = new String[] { SSHConst.STRING_EMPTY };
        }
        List<ExecResponse> execResponseList = new ArrayList<ExecResponse>(commandArr.length);
        for (String command: commandArr) {
            ExecResponse execResponse = new ExecResponse();
            execResponse.setHost(response.getHost());
            execResponse.setCommand(command);
            execResponse.setCommandCostMillis(response.getCommandCostMillis());
            execResponse.setOutputContent(SSHConst.STRING_EMPTY);
            execResponse.setErrorContent(SSHConst.STRING_EMPTY);
            if (!response.isSuccess()) {
                execResponse.setErrorContent(response.getErrorContent());
            }
            execResponseList.add(execResponse);
        }
        if (response.isSuccess()) {
            String[] outputLines = response.getOutputContentLines();
            String[] errorLines = response.getErrorContentLines();
            // 控制台正常日志解析处理
            StringBuilder outputBuilder = new StringBuilder();
            for (int i = 0, respIndex = 0; i < outputLines.length; i++) {
                String line = outputLines[i];
                // 命令输出完成
                if (SSHConst.STDOUT_LINE_SEPARATOR.equals(line)) {
                    ExecResponse execResponse = execResponseList.get(respIndex);
                    execResponse.setOutputContent(outputBuilder.toString());
                    execResponse.setSuccess(true);
                    respIndex++;
                    outputBuilder.setLength(0);
                } else {
                    if (outputBuilder.length() != 0) {
                        outputBuilder.append(SSHConst.STRING_LINE_SEPARATOR);
                    }
                    outputBuilder.append(line);
                }
            }
            // 控制台错误日志解析处理
            StringBuilder errorBuilder = new StringBuilder();
            for (int i = 0, respIndex = 0; i < errorLines.length; i++) {
                String line = errorLines[i];
                // 错误输出完成 - 命令执行完成
                if (SSHConst.STDERR_LINE_SEPARATOR.equals(line)) {
                    ExecResponse execResponse = execResponseList.get(respIndex);
                    execResponse.setErrorContent(errorBuilder.toString());
                    String errorContent = execResponse.getErrorContent();
                    if (errorContent != null && errorContent.length() > 0) {
                        execResponse.setSuccess(false);
                    }
                    respIndex++;
                    errorBuilder.setLength(0);
                } else {
                    if (errorBuilder.length() != 0) {
                        errorBuilder.append(SSHConst.STRING_LINE_SEPARATOR);
                    }
                    errorBuilder.append(line);
                }
            }
        }
        return execResponseList;
    }

}
