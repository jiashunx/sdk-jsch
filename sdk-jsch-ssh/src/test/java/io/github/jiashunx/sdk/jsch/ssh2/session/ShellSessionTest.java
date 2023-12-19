package io.github.jiashunx.sdk.jsch.ssh2.session;

import io.github.jiashunx.sdk.jsch.ssh2.resp.ShellResponse;
import io.github.jiashunx.sdk.jsch.ssh2.server.SSHServer;
import io.github.jiashunx.sdk.jsch.ssh2.server.Server;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class ShellSessionTest {

    @Test
    public void testExec() {
        Server server = new SSHServer("192.168.183.1", "jiashunx", "1234.abcd");
        try (ShellSession session = new ShellSession(server)) {
            session.execute(executor -> {
                if (!executor.isInitialized()) {
                    System.err.println("初始化失败");
                    return;
                }
                System.out.print(executor.getDefaultCmdOutput());

                String command1 = "cat /proc/meminfo";
                System.out.println(command1);
                ShellResponse shellResponse1 = executor.exec(command1);
                if (shellResponse1 != null) {
                    String content1 = shellResponse1.getContent();
                    Assert.assertNotNull(content1);
                    System.out.print(content1);
                }

                String command2 = "pwd";
                System.out.println(command2);
                ShellResponse shellResponse2 = executor.exec(command2);
                if (shellResponse2 != null) {
                    String content2 = shellResponse2.getContent();
                    Assert.assertNotNull(content2);
                    System.out.println(content2);
                }
            }, Throwable::printStackTrace);
        }
    }

}
