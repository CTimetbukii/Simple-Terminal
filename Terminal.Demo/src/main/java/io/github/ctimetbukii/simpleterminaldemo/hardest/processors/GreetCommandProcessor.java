package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.InjectOutPrintStream;

import java.io.PrintStream;

@CommandProcessor("greet")
public class GreetCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor(paraJoin = true) //用户名中可能出现空格，因此启用参数拼接
    public void greet(String user) {
        out.println("Hello, " + user);
    }
}
