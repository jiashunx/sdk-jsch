package io.github.jiashunx.sdk.jsch.ssh2.server;

import io.github.jiashunx.sdk.jsch.ssh.SSHConst;
import io.github.jiashunx.sdk.jsch.ssh2.type.AuthType;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class SSHServer extends Server {

    /**
     * 服务器监听端口.
     */
    private int port = SSHConst.DEFAULT_SSH_PORT;

    public SSHServer() {
        super();
    }

    public SSHServer(String host, int port, String username, String password) {
        this();
        this.host = Objects.requireNonNull(host);
        this.port = port;
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
    }

    public SSHServer(String host, String username, String password) {
        this(host, SSHConst.DEFAULT_SSH_PORT, username, password);
    }

    public SSHServer(String host, String username) {
        this(host, username, SSHConst.STRING_EMPTY);
    }

    @Override
    protected Server $new() {
        return new SSHServer();
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Server setPort(int port) {
        this.port = port;
        return this;
    }
}
