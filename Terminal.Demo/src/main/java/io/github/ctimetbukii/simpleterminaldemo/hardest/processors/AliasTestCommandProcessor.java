package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;

@CommandProcessor(value = "alias", alias = "A")
public class AliasTestCommandProcessor {
    @CommandExecutor
    public void alias() {
        System.out.println("a");
    }
}
