package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.InjectOutPrintStream;
import io.github.ctimetbukii.simpleterminal.flags.ClosableFlag;

import java.io.PrintStream;

@CommandProcessor("cl")
public class CloseTestCommandProcessor implements ClosableFlag {
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor
    public void cl() {
        out.println("cl");
    }
    /**
     * 关闭当前指令处理器中的资源。若没有注册自定义的stop命令，则默认的stop命令
     * 在执行时会依次调用各个指令处理器的closeResource方法。若该方法抛出了异常，则打印堆栈然后继续执行
     */
    @Override
    public void closeResources() throws Exception {
        System.out.println("test hit");
    }
}
