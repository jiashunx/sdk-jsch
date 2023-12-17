package io.github.jiashunx.sdk.jsch.ssh2.session;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import io.github.jiashunx.sdk.jsch.ssh.SSHConst;
import io.github.jiashunx.sdk.jsch.ssh2.exception.JschException;
import io.github.jiashunx.sdk.jsch.ssh2.resp.ShellResponse;
import io.github.jiashunx.sdk.jsch.ssh2.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author jiashunx
 */
public class ShellSession extends AbstractSession {

    private static final Logger logger = LoggerFactory.getLogger(ShellSession.class);

    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 执行命令后是否立即关闭session
     * true-执行命令后立即关闭，下次执行时重新连接
     * false-执行命令后不关闭session
     */
    private boolean autoClose = false;

    public ShellSession(Server server) {
        super(server);
    }

    @Override
    public void close() {
        getServer().release();
    }

    public boolean isAutoClose() {
        return autoClose;
    }

    public ShellSession setAutoClose(boolean autoClose) {
        this.autoClose = autoClose;
        return this;
    }

    public static class ShellExecutor {
        /**
         * 接收shell输出内容
         */
        private final InputStream inputStream;
        /**
         * 发送shell输入内容
         */
        private final OutputStream outputStream;
        /**
         * 输入输出流关闭标志
         */
        private volatile boolean streamClosed;
        /**
         * shell输出内容
         */
        private final LinkedBlockingQueue<String> outputQueue = new LinkedBlockingQueue<>();
        /**
         * 接收shell输出内容（增量）
         */
        private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        /**
         * 线程计数器
         */
        private static final AtomicInteger counter = new AtomicInteger(0);
        /**
         * 读写同步处理
         */
        private volatile CountDownLatch countDownLatch;
        /**
         * 额外命令
         */
        private static final String ADDITIONAL_COMMAND = SSHConst.STRING_SEMICOLON + SSHConst.COMMAND_ECHO_STDOUT_LINE;
        /**
         * 额外命令输出
         */
        private static final String ADDITIONAL_COMMAND_OUTPUT = SSHConst.STDOUT_LINE_SEPARATOR;
        /**
         * 上次执行命令内容
         */
        private volatile String prevCommand;
        /**
         * 空命令返回结果（回车）
         */
        private volatile String emptyCmdOutput;
        /**
         * 默认命令行返回结果（空命令行前缀）
         */
        private volatile String defaultCmdOutput;
        /**
         * 替换后的空命令返回结果（回车）
         */
        private volatile String replacedCmdOutput;
        public ShellExecutor(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = Objects.requireNonNull(inputStream);
            this.outputStream = Objects.requireNonNull(outputStream);
        }
        private ShellExecutor bgn() {
            new Thread(() ->{
                while (!streamClosed) {
                    readOnce();
                }
            }, "shell-read-" + counter.incrementAndGet()).start();
            write("pwd").read();
            this.emptyCmdOutput = write("").read();
            this.replacedCmdOutput = this.emptyCmdOutput;
            this.defaultCmdOutput = this.emptyCmdOutput;
            if (this.emptyCmdOutput.startsWith("\r\n\r\n")) {
                this.replacedCmdOutput = this.emptyCmdOutput.substring(2);
                this.defaultCmdOutput = this.emptyCmdOutput.substring(4);
            }
            return this;
        }
        private ShellExecutor execute(Consumer<ShellExecutor> consumer) {
            try {
                consumer.accept(this);
            } catch (Throwable throwable) {
                logger.error("shell[execute]异常", throwable);
            }
            return this;
        }
        private void fin() {
            try {
                outputStream.close();
                logger.info("shell输出流[close]成功");
            } catch (Throwable throwable) {
                logger.error("shell输出流[close]异常", throwable);
            }
            try {
                inputStream.close();
                logger.info("shell输入流[close]成功");
            } catch (Throwable throwable) {
                logger.error("shell输入流[close]异常", throwable);
            }
            streamClosed = true;
        }
        public synchronized ShellExecutor write(String command) {
            StringBuilder commandBuilder = new StringBuilder();
            if (command == null || command.trim().isEmpty()) {
                command = "echo ''";
            }
            commandBuilder.append(command.trim()).append(ADDITIONAL_COMMAND).append("\n");
            try {
                outputStream.write(commandBuilder.toString().getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                prevCommand = commandBuilder.substring(0, commandBuilder.length() - 1);
                countDownLatch = new CountDownLatch(1);
                countDownLatch.await();
            } catch (Throwable throwable) {
                logger.error("shell[write]异常", throwable);
                countDownLatch = null;
            }
            return this;
        }
        public String getDefaultCmdOutput() {
            return this.defaultCmdOutput == null ? "" : this.defaultCmdOutput;
        }
        public synchronized String read() {
            return outputQueue.poll();
        }
        private void readOnce() {
            try {
                if (countDownLatch != null && countDownLatch.getCount() == 1L) {
                    byte[] tmpBytes = new byte[1024];
                    int length = 0;
                    int available = Math.max(inputStream.available(), 0);
                    if (available > 0) {
                        int writeSize = 0;
                        while (writeSize < available) {
                            length = inputStream.read(tmpBytes);
                            byteArrayOutputStream.write(tmpBytes, 0, length);
                            writeSize = writeSize + length;
                        }
                    }
                    String content = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
                    int prevIndex = content.indexOf(prevCommand);
                    if (prevIndex >= 0) {
                        content = content.substring(prevIndex + prevCommand.length());
                        int indexA = content.indexOf(ADDITIONAL_COMMAND_OUTPUT);
                        if (indexA >= 0) {
                            int indexB = content.indexOf(ADDITIONAL_COMMAND_OUTPUT + "\r\n");
                            if (indexB >= 0) {
                                indexA = indexB;
                            }
                            content = content.substring(0, indexA) + content.substring(indexA + ADDITIONAL_COMMAND_OUTPUT.length());
                            if (content.startsWith("\r\n")) {
                                content = content.substring(2);
                            }
                            if (this.replacedCmdOutput != null) {
                                if (content.endsWith(this.emptyCmdOutput)) {
                                    content = content.replace(this.emptyCmdOutput, this.replacedCmdOutput);
                                }
                            }
                            outputQueue.offer(content);
                            byteArrayOutputStream.reset();
                            countDownLatch.countDown();
                        }
                    }
                }
                Thread.sleep(10L);
            } catch (Throwable throwable) {
                logger.error("shell[read]异常", throwable);
            }
        }
    }

    public ShellResponse execute(Consumer<ShellExecutor> consumer) {
        ShellResponse shellResponse = new ShellResponse();
        Server server = getServer();
        Session session = null;
        ChannelShell channelShell = null;
        try {
            // 获取session，自动重连
            session = server.getSession();
            channelShell = (ChannelShell) session.openChannel("shell");
            PipedInputStream pipedInputStream = new PipedInputStream();
            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            channelShell.setInputStream(new PipedInputStream(pipedOutputStream));
            channelShell.setOutputStream(new PipedOutputStream(pipedInputStream));
            channelShell.setPty(true);
            channelShell.connect(server.getChannelConnectTimeoutMillis());
            ShellExecutor executor = new ShellExecutor(pipedInputStream, pipedOutputStream);
            executor.bgn().execute(consumer).fin();
        } catch (Throwable throwable) {
            shellResponse.setSuccess(false);
            shellResponse.setErrorObj(new JschException(throwable));
        } finally {
            if (channelShell != null) {
                channelShell.disconnect();
            }
            if (isAutoClose()) {
                close();
            }
        }
        return shellResponse;
    }

}
