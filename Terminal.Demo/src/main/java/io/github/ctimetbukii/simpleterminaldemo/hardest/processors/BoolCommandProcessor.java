package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.InjectOutPrintStream;

import java.io.PrintStream;

@CommandProcessor(value = "bool", matchUpperAndLowerCase = false)
public class BoolCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor
    public void bool(boolean b) {
        out.println("b="+b);
    }
}
