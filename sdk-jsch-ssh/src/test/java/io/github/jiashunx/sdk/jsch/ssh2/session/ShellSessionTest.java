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
                System.out.print(executor.getDefaultCmdOutput());

                String command1 = "cat /proc/meminfo";
                System.out.println(command1);
                String content1 = executor.write(command1).read();
                Assert.assertNotNull(content1);
                System.out.print(content1);

                String command2 = "du";
                System.out.println(command2);
                String content2 = executor.write(command2).read();
                Assert.assertNotNull(content2);
                System.out.println(content2);
            });
        }
    }

}
