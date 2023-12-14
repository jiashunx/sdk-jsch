package io.github.jiashunx.sdk.jsch.ssh;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author jiashunx
 */
public class SSHExecutor {

    /**
     * 私有构造方法.
     */
    private SSHExecutor() {}

    /**
     * SSHHelper实例持有class.
     */
    private static class SSHHelperHolder {
        private static final SSHExecutor INSTANCE = new SSHExecutor();
    }

    /**
     * 获取SSHHelper实例对象.
     */
    private static SSHExecutor getInstance() {
        return SSHHelperHolder.INSTANCE;
    }

    /**
     * ssh执行命令.
     * @param request ssh请求对象
     * @return ssh请求响应对象
     */
    public static SSHResponse execCommand(SSHRequest request) {
        return analysizeResponse(request, getInstance().exec(request)).get(0);
    }

    /**
     * ssh执行多条命令.<br>
     * @param request ssh请求对象
     * @return 按照请求对象ssh命令顺序输出的响应结果封装对象列表
     */
    public static List<SSHResponse> execMultiCommand(SSHRequest request) {
        return analysizeResponse(request, getInstance().exec(request));
    }

    /**
     * 解析ssh请求响应结果（多条命令合并执行后对响应结果进行拆分）.
     * @param request ssh请求对象
     * @param response ssh请求响应对象
     * @return 解析后得到的ssh请求响应对象
     */
    private static List<SSHResponse> analysizeResponse(SSHRequest request, final SSHResponse response) {
        String[] commandArr = null;
        if (request != null) {
            commandArr = request.getCommandArr();
        }
        if (commandArr == null) {
            commandArr = new String[] { SSHConst.STRING_EMPTY };
        }
        List<SSHResponse> responseList = new ArrayList<SSHResponse>(commandArr.length);
        for (String command: commandArr) {
            SSHResponse resp = new SSHResponse();
            resp.setCommand(command);
            resp.setCommandCostMillis(response.getCommandCostMillis());
            resp.setRemoteHost(response.getRemoteHost());
            resp.setOutputContent(SSHConst.STRING_EMPTY);
            resp.setErrorContent(SSHConst.STRING_EMPTY);
            if (!response.isSuccess()) {
                resp.setErrorContent(response.getErrorContent());
            }
            responseList.add(resp);
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
                    SSHResponse resp = responseList.get(respIndex);
                    resp.setOutputContent(outputBuilder.toString());
                    resp.setSuccess(true);
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
                    SSHResponse resp = responseList.get(respIndex);
                    resp.setErrorContent(errorBuilder.toString());
                    String errorContent = resp.getErrorContent();
                    if (errorContent != null && errorContent.length() > 0) {
                        resp.setSuccess(false);
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
        return responseList;
    }

    /**
     * ssh执行命令.
     * @param request ssh请求对象
     * @return ssh请求响应对象
     */
    private SSHResponse exec(SSHRequest request) {
        long startMillis = System.currentTimeMillis();
        long costMillis = 0L;
        SSHResponse response = new SSHResponse();
        String command = SSHConst.STRING_NULL;
        if (request != null) {
            String[] commandArr = request.getCommandArr();
            // 多命令处理，插入标记命令
            // 对ssh命令进行格式化, 统一进行命令分隔处理（主要针对多命令执行）
            StringBuilder commandBuilder = new StringBuilder();
            for (String c: commandArr) {
                commandBuilder.append(c).append(SSHConst.STRING_SEMICOLON)
                        .append(SSHConst.COMMAND_ECHO_STDOUT_LINE).append(SSHConst.STRING_SEMICOLON)
                        .append(SSHConst.COMMAND_ECHO_STDERR_LINE).append(SSHConst.STRING_SEMICOLON);
            }
            command = commandBuilder.toString();
            response.setCommand(command);
            response.setRemoteHost(request.getRemoteHost());
        }
        Session session = null;
        ChannelExec channelExec = null;
        ByteArrayOutputStream outputStream = null;
        ByteArrayOutputStream errorOutputStream = null;
        try {
            JSch jSch = new JSch();
            session = jSch.getSession(request.getUsername(), request.getRemoteHost(), request.getSshPort());
            Properties properties = new Properties();
            properties.put("StrictHostKeyChecking", "no");
            session.setConfig(properties);
            session.setPassword(request.getPassword());
            session.setTimeout(request.getSessionTimeoutMillis()); // 毫秒
            session.connect();
            channelExec = (ChannelExec) session.openChannel("exec");
            channelExec.setCommand(command);
            outputStream = new ByteArrayOutputStream();
            errorOutputStream = new ByteArrayOutputStream();
            channelExec.setInputStream(null);
            channelExec.setErrStream(errorOutputStream);
            channelExec.setOutputStream(outputStream);
            channelExec.connect(request.getChannelConnectTimeoutMillis()); // 毫秒
            while (!channelExec.isClosed()) {
                Thread.sleep(50L);
            }
            response.setSuccess(true);
            response.setErrorContent(errorOutputStream.toString());
            response.setOutputContent(outputStream.toString());
            costMillis = System.currentTimeMillis() - startMillis;
        } catch (Throwable e) {
            response.setSuccess(false);
            response.setErrorContent(e);
            costMillis = System.currentTimeMillis() - startMillis;
        } finally {
            if (outputStream != null) {
                try { outputStream.close(); } catch (Exception e2) {}
            }
            if (errorOutputStream != null) {
                try { errorOutputStream.close(); } catch (Exception e2) {}
            }
            if (channelExec != null) {
                channelExec.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
            response.setCommandCostMillis(costMillis);
        }
        return response;
    }

}
