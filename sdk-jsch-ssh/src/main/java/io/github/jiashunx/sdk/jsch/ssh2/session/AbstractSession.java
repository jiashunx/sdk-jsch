package io.github.jiashunx.sdk.jsch.ssh2.session;

import io.github.jiashunx.sdk.jsch.ssh2.server.Server;

import java.util.Objects;

/**
 * @author jiashunx
 */
public abstract class AbstractSession implements AutoCloseable {

    protected final Server server;

    public AbstractSession(Server server) {
        this.server = Objects.requireNonNull(server);
    }

    public Server getServer() {
        return server;
    }

    public abstract void close();

}
