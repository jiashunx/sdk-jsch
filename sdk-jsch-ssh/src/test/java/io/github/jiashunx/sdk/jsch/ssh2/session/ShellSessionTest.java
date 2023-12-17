package io.github.jiashunx.sdk.jsch.ssh2.session;

import io.github.jiashunx.sdk.jsch.ssh2.server.SSHServer;
import io.github.jiashunx.sdk.jsch.ssh2.server.Server;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class ShellSessionTest {

    @Test
    public void testExecute() {
        Server server = new SSHServer("192.168.183.1", "jiashunx", "1234.abcd");
        try (ShellSession session = new ShellSession(server)) {
            session.execute(executor -> {
                String content1 = executor.write("cat /proc/cpuinfo").read();
                Assert.assertNotNull(content1);
                System.out.println("content1=====>\n" + content1 + "\n<=====content1");
                String content2 = executor.write("cat/proc/meminfo").read();
                Assert.assertNotNull(content2);
                System.out.println("content2=====>\n" + content2 + "\n<=====content2");
            });
        }
    }

}
