package io.github.ctimetbukii.simpleterminal.defaults;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.InjectOutPrintStream;

import java.io.PrintStream;
import java.util.Locale;

@CommandProcessor("help")
public class DefaultHelpCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor
    public void help(String command) {
//        out.println(command);
        //TODO help
    }

    @CommandExecutor
    public void help() {
        String getAllHelp;
        if (Locale.getDefault().getDisplayCountry().equals("China")) {
            getAllHelp = "键入 'help ?' 以获取所有指令描述。";
        } else {
            getAllHelp = "Get all command description by entering 'help ?'";
        }
        out.println(getAllHelp);
    }
}
