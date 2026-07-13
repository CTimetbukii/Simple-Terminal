package io.github.ctimetbukii.simpleterminal.defaults;

import io.github.ctimetbukii.simpleterminal.annotations.*;
import io.github.ctimetbukii.simpleterminal.GlobalTransmitter;
import io.github.ctimetbukii.simpleterminal.exceptions.IllogicalPanicException;
import io.github.ctimetbukii.simpleterminal.utils.records.CommandInformation;
import io.github.ctimetbukii.simpleterminal.utils.records.TransmitterKey;

import java.io.PrintStream;
import java.util.*;

@CommandProcessor(value = "help", descriptionKey = "simpleterminal.defaults.help")
public class DefaultHelpCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @InjectCommandDescriptionProperties
    private Properties commandDescriptions;

    @InjectDefaultDescriptionProperties
    private Properties defaultDescriptions;

    @CommandExecutor
    public void help(String command) {
//        out.println(command);
        //TODO help
    }

    @CommandExecutor
    public void help() {
        // 非常非常非常捡漏的i18n
        String getTargetHelp;
        if (Locale.getDefault().getLanguage().equals("zh")) {
            getTargetHelp = "键入 'help [命令名]' 以获取指定指令的详细信息。";
        } else {
            getTargetHelp = "Get details of the target command by entering 'help [command]'";
        }

        //打印所有命令
        @SuppressWarnings("unchecked")
        Set<String> allCommands = (Set<String>) GlobalTransmitter.getTransmitter().get(TransmitterKey.ALL_COMMANDS);
        //先获取所有命令中，最长的那个，用那个做间隔
        Optional<String> maxLengthCommandOptional = allCommands.stream().max(Comparator.comparingInt(String::length));

        int commandColumnLength = 8; //默认命令 help 和 stop 都是4个长度。再加4个做间隔，得到默认值8
        if (maxLengthCommandOptional.isPresent()
            && maxLengthCommandOptional.get().length() > 4) {
            commandColumnLength = maxLengthCommandOptional.get().length() + 4;
        }

        String[] commandsSorted = allCommands.toArray(new String[0]);
        Arrays.sort(commandsSorted);

        //打印
        for (String command : commandsSorted) {
            out.print(command);

            //打印间隔空格
            for (int i = 0; i < (commandColumnLength - command.length()); i++) {
                out.print(" ");
            }

            //拿information
            CommandInformation cInfo = GlobalTransmitter.getCommandInformationMap().get(command);
            if (cInfo == null) {
                throw new IllogicalPanicException("Why cInfo is null?");
            }

            String dsKey = cInfo.getProcessorAnn().descriptionKey();

            String description;
            //先检查在default中有没有
            if (defaultDescriptions.containsKey(dsKey)) {
                description = defaultDescriptions.getProperty(dsKey);
            } else {
                description = commandDescriptions.getProperty(dsKey);
            }

            out.println(description);
        }

        out.println();
        out.println(getTargetHelp);
    }
}
