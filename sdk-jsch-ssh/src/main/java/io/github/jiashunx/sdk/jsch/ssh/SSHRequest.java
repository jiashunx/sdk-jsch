package io.github.jiashunx.sdk.jsch.ssh;

import java.util.Arrays;

/**
 * @author jiashunx
 */
public class SSHRequest {

    /**
     * 远程服务器IP.
     */
    private String remoteHost;
    /**
     * 登录用户名.
     */
    private String username;
    /**
     * 登录用户密码.
     */
    private String password;
    /**
     * 多命令数组.
     */
    private String[] commandArr;
    /**
     * ssh创建session时的连接超时毫秒数.
     */
    private int sessionTimeoutMillis = SSHConst.DEFAULT_SESSION_TIMEOUT_MILLIS;
    /**
     * ssh命令执行时连接超时毫秒数.
     */
    private int channelConnectTimeoutMillis = SSHConst.DEFAULT_CHANNEL_CONNECT_TIMEOUT_MILLIS;
    /**
     * ssh连接端口.
     */
    private int sshPort = SSHConst.DEFAULT_SSH_PORT;

    /**
     * 构造方法.
     */
    public SSHRequest() {}

    /**
     * 构造方法(默认ssh端口22).
     * @param remoteHost 远程服务器IP
     * @param username 服务器登录用户名
     * @param password 服务器登录密码
     * @param command 远程命令(首个执行)
     * @param commands 命令数组（支持多命令）
     */
    public SSHRequest(String remoteHost, String username, String password, String command, String...commands) {
        this(remoteHost, SSHConst.DEFAULT_SSH_PORT, username, password, command, commands);
    }

    /**
     * 构造方法.
     * @param remoteHost 远程服务器IP
     * @param sshPort ssh端口
     * @param username 服务器登录用户名
     * @param password 服务器登录密码
     * @param command 远程命令(首个执行)
     * @param commands 命令数组（支持多命令）
     */
    public SSHRequest(String remoteHost, int sshPort, String username, String password, String command, String...commands) {
        this(remoteHost, sshPort, username, password, mergeCommand(command, commands));
    }

    private static String[] mergeCommand(String command, String...commands) {
        String[] commandArr = new String[commands.length + 1];
        commandArr[0] = command;
        for (int i = 0; i < commands.length; i++) {
            commandArr[i + 1] = commands[i];
        }
        return commandArr;
    }

    /**
     * 构造方法(默认ssh端口22).
     * @param remoteHost 远程服务器IP
     * @param username 服务器登录用户名
     * @param password 服务器登录密码
     * @param commands 命令数组（支持多命令）
     */
    public SSHRequest(String remoteHost, String username, String password, String[] commands) {
        this(remoteHost, SSHConst.DEFAULT_SSH_PORT, username, password, commands);
    }

    /**
     * 构造方法.
     * @param remoteHost 远程服务器IP
     * @param sshPort ssh端口
     * @param username 服务器登录用户名
     * @param password 服务器登录密码
     * @param commands 命令数组（支持多命令）
     */
    public SSHRequest(String remoteHost, int sshPort, String username, String password, String[] commands) {
        this.remoteHost = remoteHost;
        this.sshPort = sshPort;
        this.username = username;
        this.password = password;
        commandArr = null;
        if (commands != null && commands.length > 0) {
            commandArr = new String[commands.length];
            for (int i = 0; i < commands.length; i++) {
                // 命令元素中存在多个命令，只取第一个命令
                commandArr[i] = commands[i];
            }
        }
        if (commandArr == null) {
            commandArr = new String[] { SSHConst.STRING_NULL };
        }
    }

    public SSHRequest(SSHRequest request) {
        this.remoteHost = request.remoteHost;
        this.sshPort = request.sshPort;
        this.username = request.username;
        this.password = request.password;
        this.sessionTimeoutMillis = request.sessionTimeoutMillis;
        this.channelConnectTimeoutMillis = request.channelConnectTimeoutMillis;
        String[] commandSrc = request.getCommandArr();
        this.commandArr = new String[commandSrc.length];
        System.arraycopy(commandSrc, 0, this.commandArr, 0, commandSrc.length);
    }

    @Override
    public String toString() {
        return "{remoteHost=\"" + remoteHost + "\", username=\"" + username + "\", sshPort=" + sshPort
                + ", commandArr=" + Arrays.asList(getCommandArr())
                + ", sessionTimeoutMillis=" + sessionTimeoutMillis
                + ", channelConnectTimeoutMillis=" + channelConnectTimeoutMillis + "}";
    }

    public String getRemoteHost() {
        return remoteHost;
    }
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public int getSshPort() {
        return sshPort;
    }
    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }
    public String[] getCommandArr() {
        if (commandArr == null || commandArr.length == 0) {
            commandArr = new String[] { SSHConst.STRING_NULL };
        }
        return commandArr;
    }
    public int getSessionTimeoutMillis() {
        return sessionTimeoutMillis;
    }
    public void setSessionTimeoutMillis(int sessionTimeoutMillis) {
        this.sessionTimeoutMillis = sessionTimeoutMillis;
    }
    public int getChannelConnectTimeoutMillis() {
        return channelConnectTimeoutMillis;
    }
    public void setChannelConnectTimeoutMillis(int channelConnectTimeoutMillis) {
        this.channelConnectTimeoutMillis = channelConnectTimeoutMillis;
    }

}
