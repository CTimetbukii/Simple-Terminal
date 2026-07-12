package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.InjectErrPrintStream;

import java.io.PrintStream;

@CommandProcessor("err")
public class ErrTestCommandProcessor {
    @InjectErrPrintStream
    private PrintStream err;

    @CommandExecutor
    public void err() {
        err.println("此命令用于测试InjectErrPrintStream注解");
    }
}
