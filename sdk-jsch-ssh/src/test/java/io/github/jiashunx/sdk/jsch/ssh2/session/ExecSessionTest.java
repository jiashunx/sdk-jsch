package io.github.jiashunx.sdk.jsch.ssh2.session;

import io.github.jiashunx.sdk.jsch.ssh2.resp.ExecResponse;
import io.github.jiashunx.sdk.jsch.ssh2.server.SSHServer;
import io.github.jiashunx.sdk.jsch.ssh2.server.Server;
import io.github.jiashunx.sdk.jsch.ssh2.type.AuthType;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class ExecSessionTest {

    @Test
    public void testExec() {
        Server server = new SSHServer("192.168.183.1", "jiashunx", "1234.abcd");
        try (ExecSession session = new ExecSession(server)) {
            ExecResponse response1 = session.exec("asdf");
            Assert.assertFalse(response1.isSuccess());
            System.out.println(response1.getErrorContent());
            ExecResponse response2 = session.exec("cat /proc/cpuinfo");
            Assert.assertTrue(response2.isSuccess());
            System.out.println(response2.getOutputContent());
        }
    }

    @Test
    public void testExec_autoClose() {
        Server server = new SSHServer("192.168.183.1", "jiashunx", "1234.abcd");
        try (ExecSession session = new ExecSession(server).setAutoClose(true)) {
            ExecResponse response1 = session.exec("asdf");
            Assert.assertFalse(response1.isSuccess());
            System.out.println(response1.getErrorContent());
            ExecResponse response2 = session.exec("cat /proc/cpuinfo");
            Assert.assertTrue(response2.isSuccess());
            System.out.println(response2.getOutputContent());
        }
    }

    @Test
    public void testExec_WithPublicKey() {
        Server server = new SSHServer("192.168.183.1", "root")
                .setAuthType(AuthType.PUBLIC_KEY)
                .setPublicKeyFilePath("C:\\192.168.183.1");
        try (ExecSession session = new ExecSession(server)) {
            ExecResponse response1 = session.exec("asdf");
            Assert.assertFalse(response1.isSuccess());
            System.out.println(response1.getErrorContent());
            ExecResponse response2 = session.exec("cat /proc/cpuinfo");
            Assert.assertTrue(response2.isSuccess());
            System.out.println(response2.getOutputContent());
        }
    }

}
