package io.github.jiashunx.sdk.jsch.ssh;

/**
 * @author jiashunx
 */
public class SSHConst {

    private SSHConst() {}

    /**
     * 默认的ssh连接端口.
     */
    public static final int DEFAULT_SSH_PORT = 22;

    /**
     * 默认的ssh创建session时的连接超时毫秒数.
     */
    public static final int DEFAULT_SESSION_TIMEOUT_MILLIS = 3000;

    /**
     * 默认的ssh命令执行时连接超时毫秒数.
     */
    public static final int DEFAULT_CHANNEL_CONNECT_TIMEOUT_MILLIS = 3000;

    /**
     * 标准输出分隔符（用于分隔正常输出的文本内容）.
     */
    public static final String STDOUT_LINE_SEPARATOR = "STDOUT-LINE-SEPARATOR";

    /**
     * 错误输出分隔符（用于分隔错误输出的文本内容）.
     */
    public static final String STDERR_LINE_SEPARATOR = "STDERR-LINE-SEPARATOR";

    /**
     * 分隔符标准输出命令.
     */
    public static final String COMMAND_ECHO_STDOUT_LINE = "echo '" + STDOUT_LINE_SEPARATOR + "'>&1";

    /**
     * 分隔符异常输出命令.
     */
    public static final String COMMAND_ECHO_STDERR_LINE = "echo '" + STDERR_LINE_SEPARATOR + "'>&2";

    /**
     * null字符串.
     */
    public static final String STRING_NULL = "null";

    /**
     * 空字符串.
     */
    public static final String STRING_EMPTY = "";

    /**
     * 空格.
     */
    public static final String STRING_BLANK = " ";

    /**
     * 未知属性字符串.
     */
    public static final String STRING_UNKNOWN = "unknown";

    /**
     * 分号.
     */
    public static final String STRING_SEMICOLON = ";";

    /**
     * 冒号.
     */
    public static final String STRING_COLON = ":";

    /**
     * 换行符.
     */
    public static final String STRING_LINE_SEPARATOR = "\n";

    /**
     * tab字符串.
     */
    public static final String STRING_TAB = "\t";

}
