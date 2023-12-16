package io.github.jiashunx.sdk.jsch.ssh2.server;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import io.github.jiashunx.sdk.jsch.ssh.SSHConst;
import io.github.jiashunx.sdk.jsch.ssh2.type.AuthType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author jiashunx
 */
public abstract class Server {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    /**
     * 服务器IP.
     */
    protected String host = "";

    /**
     * 登录用户名.
     */
    protected String username = "";

    /**
     * 登录用户密码（认证类型为PASSWORD时必输）.
     */
    protected String password = "";

    /**
     * 认证类型（默认为PASSWORD）
     */
    protected AuthType authType = AuthType.PASSWORD;

    /**
     * 登陆publicKey文件路径（认证类型为PUBLIC_KEY时必输）.
     */
    protected String publicKeyFilePath = "";

    /**
     * session连接超时时间（毫秒）
     */
    protected int sessionConnectTimeoutMillis = SSHConst.DEFAULT_SESSION_CONNECT_TIMEOUT_MILLIS;

    /**
     * channel连接超时时间（毫秒）
     */
    protected int channelConnectTimeoutMillis = SSHConst.DEFAULT_CHANNEL_CONNECT_TIMEOUT_MILLIS;

    /**
     * jsch sesstion对象.
     */
    protected volatile Session session;

    protected abstract Server $new();

    public Server copy() {
        return $new()
                .setHost(getHost())
                .setPort(getPort())
                .setUsername(getUsername())
                .setPassword(getPassword())
                .setAuthType(getAuthType())
                .setPublicKeyFilePath(getPublicKeyFilePath())
                .setSessionConnectTimeoutMillis(getSessionConnectTimeoutMillis());
    }

    @Override
    public String toString() {
        return "Server{" +
                "host='" + host + '\'' +
                ", port='" + getPort() + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", authType=" + authType +
                ", publicKeyFilePath='" + publicKeyFilePath + '\'' +
                ", sessionConnectTimeoutMillis=" + sessionConnectTimeoutMillis +
                ", session=" + session +
                '}';
    }

    public String getHost() {
        return host;
    }

    public Server setHost(String host) {
        this.host = Objects.requireNonNull(host);
        return this;
    }

    public abstract int getPort();

    public abstract Server setPort(int port);

    public String getUsername() {
        return username;
    }

    public Server setUsername(String username) {
        this.username = Objects.requireNonNull(username);
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Server setPassword(String password) {
        this.password = Objects.requireNonNull(password);
        return this;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public Server setAuthType(AuthType authType) {
        this.authType = Objects.requireNonNull(authType);
        return this;
    }

    public String getPublicKeyFilePath() {
        return publicKeyFilePath;
    }

    public Server setPublicKeyFilePath(String publicKeyFilePath) {
        this.publicKeyFilePath = Objects.requireNonNull(publicKeyFilePath);
        return this;
    }

    public int getSessionConnectTimeoutMillis() {
        return sessionConnectTimeoutMillis;
    }

    public Server setSessionConnectTimeoutMillis(int sessionConnectTimeoutMillis) {
        this.sessionConnectTimeoutMillis = sessionConnectTimeoutMillis;
        return this;
    }

    public int getChannelConnectTimeoutMillis() {
        return channelConnectTimeoutMillis;
    }

    public Server setChannelConnectTimeoutMillis(int channelConnectTimeoutMillis) {
        this.channelConnectTimeoutMillis = channelConnectTimeoutMillis;
        return this;
    }

    public synchronized Session getSession() {
        if (session == null) {
            session = createSession();
        }
        return connectSession(session, getSessionConnectTimeoutMillis());
    }

    public synchronized Server release() {
        disconnectSession(session);
        return this;
    }

    private Session createSession() {
        Session session = null;
        try {
            JSch jSch = new JSch();
            if (getAuthType() == AuthType.PUBLIC_KEY) {
                jSch.addIdentity(getPublicKeyFilePath());
            }
            session = jSch.getSession(getUsername(), getHost(), getPort());
            session.setConfig("userauth.gssapi-with-mic", "no");
            session.setConfig("StrictHostKeyChecking", "no");
            switch (getAuthType()) {
                case PASSWORD:
                    session.setConfig("PreferredAuthentications", "password");
                    session.setPassword(getPassword());
                    break;
                case PUBLIC_KEY:
                    session.setConfig("PreferredAuthentications", "publickey");
                    break;
            }
            logger.info("session[create]成功: server={}", this);
            session = connectSession(session, getSessionConnectTimeoutMillis());
        } catch (Throwable throwable) {
            logger.error("session[create]异常: server={}", this, throwable);
            disconnectSession(session);
            session = null;
        }
        return session;
    }

    private Session connectSession(Session session) {
        return connectSession(session, -1);
    }

    private Session connectSession(Session session, int connectTimeoutMillis) {
        if (session != null) {
            if (!session.isConnected()) {
                try {
                    if (connectTimeoutMillis > 0) {
                        session.connect(connectTimeoutMillis);
                    } else {
                        session.connect();
                    }
                    logger.info("session[connect]成功: connectTimeoutMillis={}, session={}", connectTimeoutMillis, session);
                } catch (Throwable throwable) {
                    if (throwable instanceof JSchException && throwable.getMessage().equals("Packet corrupt")) {
                        logger.warn("session[connect]异常: {}, 需重建session", throwable.getMessage());
                        this.session = connectSession(this.createSession(), getSessionConnectTimeoutMillis());
                        return this.session;
                    } else {
                        logger.error("session[connect]异常: connectTimeoutMillis={}, session={}", connectTimeoutMillis, session, throwable);
                    }
                }
            }
        }
        return session;
    }

    private Session disconnectSession(Session session) {
        if (session != null) {
            if (session.isConnected()) {
                try {
                    session.disconnect();
                    logger.info("session[disconnect]成功, session: {}", session);
                } catch (Throwable throwable) {
                    logger.error("session[disconnect]异常, session={}", session, throwable);
                }
            }
        }
        return session;
    }
}
