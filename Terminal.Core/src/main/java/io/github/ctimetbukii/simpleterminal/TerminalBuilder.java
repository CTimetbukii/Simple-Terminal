package io.github.ctimetbukii.simpleterminal;

import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class TerminalBuilder {
    private final TerminalOptions options = new TerminalOptions();

    /**
     * 获取一个 TerminalBuilder 对象。 与 'new TerminalBuilder()' 等价。<br/>
     * @return TerminalBuilder 对象
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull TerminalBuilder builder() {
        return new TerminalBuilder();
    }

    /**
     * 设置终端在另一个线程异步运行。默认为同步运行
     * @return TerminalBuilder 实例
     */
    public TerminalBuilder runAsync() {
        options.setRunAsync(true);
        return this;
    }

    /**
     * 设置终端执行的异步线程为守护线程。该设置仅当runAsync被调用后有效。默认为非守护线程。
     * @return TerminalBuilder 实例
     */
    public TerminalBuilder asDamon() {
        options.setDamon(true);
        return this;
    }

    /**
     * 设置终端执行的异步线程名。该设置仅当runAsync被调用后有效。默认为 CONSOLE
     * @param name 端执行的异步线程名
     * @return TerminalBuilder 实例
     */
    public TerminalBuilder setTerminalThreadName(String name) {
        options.setTerminalThreadName(name);
        return this;
    }

    /**
     * 设置终端的输入流。默认为System.in。可以通过调用此方法重设输入流
     * @param input 终端输入流
     * @return TerminalBuilder 实例
     */
    public TerminalBuilder setTerminalInputStream(InputStream input) {
        options.setInputStream(input);
        return this;
    }

    /**
     * 设置终端的输出流。默认为System.out。可以通过调用此方法重设输出流
     * @param output 终端输出流
     * @return TerminalBuilder 实例
     */
    public TerminalBuilder setTerminalOutputStream(PrintStream output) {
        options.setOutputStream(output);
        return this;
    }

    /**
     * 若使用了runAsync，则检查ExecutorService值。若该值为null，则Simple-Terminal将new一个新的线程并在该线程内执行终端监听代码。
     * 若该值不为null，则向该ExecutorService中submit终端监听代码。
     * @param service ExecutorService 对象
     * @return TerminalBuilder 对象
     */
    public TerminalBuilder setExecutorService(ExecutorService service) {
        options.setRunExecutor(service);
        return this;
    }

    /**
     * 该方法用于设置用于锚定命令处理器的包路径。该方法必须被调用。更建议使用setAnchorPackageClassHelper方法传入一个用于锚定的类，这样在重构时更加方便。<br/>
     * Simple-Terminal靠反射查找带有注解标记的方法或类，因此用于锚定命令处理器的包路径非常重要。<br/>
     * 例如，若将带有CommandExecutorMethod的方法的类放在com.example下，则传入值应当为com.example。
     * 这表明Simple-Terminal将从com.example包下开始扫描有无带有相关注解的类/方法。该值不得为null
     * @param anchorPackagePath 用于锚定命令处理器的包路径。
     */
    public TerminalBuilder setAnchorPackagePath(@NonNull String anchorPackagePath) {
        options.setAnchorPackagePath(anchorPackagePath);
        return this;
    }

    /**
     * 由于setAnchorPackagePath方法传入的是一个字符串值，若遇到重构，将类所在的包重命名或移动则还需要一并修改传入的值，较为麻烦。
     * 该方法则使用锚定类的包路径，使得方法调用不受重构影响，更加方便。其他有关anchorPackagePath的内容参见#setAnchorPackagePath
     * @param classHelper 用于锚定的类。类的内容不重要，但该类必须被放置在有命令注解的包下
     * @return TerminalBuilder实例
     */
    public TerminalBuilder setAnchorPackageClassHelper(Class<?> classHelper) {
        options.setAnchorPackagePath(classHelper.getPackage().getName());
        return this;
    }

    /**
     * 设置命令不存在时的命令处理器。当输入的命令不存在时，调用传入的processor，并将命令传入该processor的accept方法。
     * @param processor 命令不存在时的命令处理器
     * @return TerminalBuilder 实例
     */
    public TerminalBuilder setCommandDoesntExistProcessor(Consumer<String> processor) {
        options.setCommandDoesntExistProcessor(processor);
        return this;
    }

    /**
     * 设置stopRunnable。stopRunnable即stop指令执行时需要处理的内容。当stop指令执行时，会首先调用各个ClosableFlag的closeResource，
     * 然后调用此stopRunnable，用户可以在这里加入其自身的一些关闭内容。随后，stop指令将调用System.exit使进程退出
     */
    public TerminalBuilder setStopRunnable(Runnable stopRunnable) {
        options.setStopRunnable(stopRunnable);
        return this;
    }

    /**
     * 设置用户自定义命令的descriptions的properties文件的资源目录与文件前缀。Simple-Terminal会在
     * resourceDir 下尝试查找带有此前缀的资源文件。资源目录不带前后斜杠。<br/>
     * 除了文件前缀外，Simple-Terminal还会根据Language Code尝试查找
     * 对应语言的文件。基本的格式与查找顺序是:<br/>
     * <code>文件前缀_语言编码.properties</code><br/>
     * <code>文件前缀.properties</code><br/>
     * 例如，filePrefix 为"descriptions"，语言编码为"zh"时，Simple-Terminal会依次尝试在
     * 资源目录下查找下面的文件：<br/>
     * <code>descriptions_zh.properties</code><br/>
     * <code>descriptions.properties</code><br/>
     * 语言编码由Locale.getDefault().getLanguage()返回，
     * 以此实现国际化资源加载。用户不必再手动设置
     */
    public TerminalBuilder setDescriptionsPath(String resourceDir, String filePrefix) {
        if (resourceDir == null || filePrefix == null) {
            throw new IllegalArgumentException("resourceDir & filePrefix cannot be null! use \"\" instead of null!");
        }

        resourceDir = resourceDir.replace('\\', '/').replaceAll("^/+|/+$", "");
        options.setDescriptionsResourceDir(resourceDir);
        options.setDescriptionsFilePrefix(filePrefix);
        return this;
    }

    /**
     * 构造Terminal对象
     * @return Terminal 对象
     */
    public Terminal build() {
        return new Terminal(options);
    }
}
