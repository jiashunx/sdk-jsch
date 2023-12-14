package io.github.jiashunx.sdk.jsch.ssh;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author jiashunx
 */
public class SSHExecutorTest {

    @Test
    public void testExecCommand() {
        SSHRequest request1 = new SSHRequest("192.168.183.1", "jiashunx", "1234.abcd", "asdf");
        SSHResponse response1 = SSHExecutor.execCommand(request1);
        Assert.assertFalse(response1.isSuccess());
        SSHRequest request2 = new SSHRequest("192.168.183.1", "jiashunx", "1234.abcd", "cat /proc/cpuinfo");
        SSHResponse response2 = SSHExecutor.execCommand(request2);
        Assert.assertTrue(response2.isSuccess());
        System.out.println(response2.getOutputContent());
    }

}
