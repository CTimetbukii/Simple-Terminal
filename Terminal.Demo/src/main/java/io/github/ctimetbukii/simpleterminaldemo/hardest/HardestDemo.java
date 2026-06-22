package io.github.ctimetbukii.simpleterminaldemo.hardest;

import io.github.ctimetbukii.simpleterminal.Terminal;
import io.github.ctimetbukii.simpleterminal.TerminalBuilder;
import io.github.ctimetbukii.simpleterminaldemo.hardest.processors.InfoCommandProcessor;

public class HardestDemo {
    public static void main(String[] args) {
        Terminal terminal = TerminalBuilder.builder()
                .setAnchorPackageClassHelper(InfoCommandProcessor.class)
                .build();
        terminal.run();
    }
}
