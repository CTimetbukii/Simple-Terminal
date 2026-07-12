package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.InjectOutPrintStream;
import io.github.ctimetbukii.simpleterminal.annotations.SubCommandExecutor;

import java.io.PrintStream;

@CommandProcessor("info")
public class InfoCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor
    public void info() {
        out.println("'info command' called. method#info called.");
    }

    /**
     * 该方法将在执行 info cpu 时触发。其中info，cpu中的任何一个或多个字母换成大写仍然可以触发。
     */
    @SubCommandExecutor("cpu")
    public void infoCpu() {
        out.println("'info cpu' command called. method#infoCpu called.");
    }

    /**
     * 该方法将在执行 info mem 时触发。其中info中的任何一个或多个字母换成大写仍然可以触发，
     * 但mem必须为全小写形式，因为matchUpperAndLowerCase = false，此时必须按value中的形式严格匹配。
     */
    @SubCommandExecutor(value = "mem", matchUpperAndLowerCase = false)
    public void infoMem() {
        out.println("'info mem' command called. method#infoMem called.");
    }
}
