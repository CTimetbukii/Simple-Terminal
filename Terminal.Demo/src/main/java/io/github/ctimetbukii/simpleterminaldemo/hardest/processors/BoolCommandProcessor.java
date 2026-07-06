package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;

@CommandProcessor(value = "bool", matchUpperAndLowerCase = false)
public class BoolCommandProcessor {
    @CommandExecutor
    public void bool(boolean b) {
        System.out.println("b="+b);
    }
}
