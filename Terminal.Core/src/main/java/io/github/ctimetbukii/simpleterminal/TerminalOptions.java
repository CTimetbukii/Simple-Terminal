package io.github.ctimetbukii.simpleterminal;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class TerminalOptions {
    /**
     * 该终端是否需要在其他线程异步执行
     */
    @Getter
    @Setter
    private boolean runAsync = false;

    /**
     * 该终端线程是否为保护线程。该值仅当runAsync为true且runExecutor为null时启用
     */
    @Getter
    @Setter
    private boolean isDamon = false;

    /**
     * 该终端线程的名。该值仅当runAsync为true且runExecutor为null时启用。
     */
    @Getter
    @Setter
    private String terminalThreadName = "CONSOLE";

    /**
     * 若设置了runAsync为true，则检查该值。若该值为null，则Simple-Terminal将new一个新的线程并在该线程内执行终端监听代码。
     * 若该值不为null，则向该ExecutorService中submit终端监听代码。
     */
    @Getter
    @Setter
    private ExecutorService runExecutor = null;

    /**
     * 终端输入流
     */
    @Getter
    @Setter
    private InputStream inputStream = System.in;

    /**
     * 终端输出流
     */
    @Getter
    @Setter
    private PrintStream outputStream = System.out;

    /**
     * 终端错误输出流
     */
    @Getter
    @Setter
    private PrintStream errOutputStream = System.err;

    /**
     * 用于锚定命令处理器的包路径。例如，若将带有CommandExecutorMethod的方法的类放在com.example下，则该值应当为com.example。
     * 这表明Simple-Terminal将从com.example包下开始扫描有无带有相关注解的类/方法。该值不得为null
     */
    @Getter
    @Setter
    @NonNull
    private String anchorPackagePath;

    /**
     * 当命令不存在时处理不存在的命令。
     */
    @Getter
    @Setter
    private Consumer<String> commandDoesntExistProcessor = cmd ->
            getErrOutputStream().println("Command '" + cmd + "' doesn't exists.");

    /**
     * 各个命令的解释映射。设置了descriptionKey时，help指令从这里获取命令解释
     */
    @Getter
    @Setter
    private Properties commandDescriptions;

    /**
     * stop指令执行时需要处理的内容。当stop指令执行时，会首先调用各个ClosableFlag的closeResource，
     * 然后调用此stopRunnable，用户可以在这里加入其自身的一些关闭内容。随后，stop指令将调用System.exit使进程退出
     */
    @Getter
    @Setter
    private Runnable stopRunnable;
}
