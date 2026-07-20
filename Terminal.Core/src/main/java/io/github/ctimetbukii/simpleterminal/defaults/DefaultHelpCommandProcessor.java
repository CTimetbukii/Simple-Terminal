package io.github.ctimetbukii.simpleterminal.defaults;

import io.github.ctimetbukii.simpleterminal.TerminalOptions;
import io.github.ctimetbukii.simpleterminal.annotations.*;
import io.github.ctimetbukii.simpleterminal.GlobalTransmitter;
import io.github.ctimetbukii.simpleterminal.exceptions.IllogicalPanicException;
import io.github.ctimetbukii.simpleterminal.utils.records.*;

import java.io.PrintStream;
import java.lang.reflect.Parameter;
import java.util.*;

@CommandProcessor(value = "help", descriptionKey = "simpleterminal.defaults.help")
public class DefaultHelpCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @InjectErrPrintStream
    private PrintStream err;

    @InjectCommandDescriptionProperties
    private Properties commandDescriptions;

    @InjectDefaultDescriptionProperties
    private Properties defaultDescriptions;

    @InjectTerminalOption
    private TerminalOptions options;

    private final String noAvailableDescription = isUseChinese() ? "暂无可用描述" : "No available descriptions";
    private final String commonUseFlag = isUseChinese() ? "普通用法" : "Common use";
    private final String subCommandFlag = isUseChinese() ? "子命令" : "Subcommands";

    @CommandExecutor(descriptionKey = "simpleterminal.defaults.help.target")
    public void help(@ParaDsKey("simpleterminal.defaults.help.para.command") String command) {
        Set<String> allCommands = GlobalTransmitter.getCommandInformationMap().keySet();

        //检查方法是否存在
        if (!allCommands.contains(command)) {
            err.printf(options.getDefaultCmdDoesntExistMessage() + "%n", command);
            return;
        }

        CommandInformation cInfo = GlobalTransmitter.getCommandInformationMap().get(command);
        if (cInfo == null) {
            throw new IllogicalPanicException("Why cInfo is null?");
        }

        String description = getDescription(cInfo.getProcessorAnn().descriptionKey());

        out.println(description);
        out.println();

        //打印用法，包括普通的CommandExecutor，和子命令SubCommandExecutor和上下文命令ContextCommandExecutor

        ExecutorInformation[] executorInformationArray = cInfo.getExecutorInformationSet().toArray(new ExecutorInformation[0]);
        Arrays.sort(executorInformationArray);

        //接下来的每轮打印，都要先打印2个空格做缩进，这样好看
        String cmdPrefix = "  " + command + " ";
        String paraDsKey;

        if (executorInformationArray.length != 0) {
            //打印普通用法
            out.println(commonUseFlag);
            for (ExecutorInformation information : executorInformationArray) {
                out.print(cmdPrefix);
                for (Parameter parameter : information.getParameters()) {
                    if (parameter.isAnnotationPresent(ParaDsKey.class)) {
                        paraDsKey = parameter.getAnnotation(ParaDsKey.class).value();
                        out.print("[" + getDescription(paraDsKey) + "] ");
                    } else {
                        out.print("[" + parameter.getName() + "] ");
                    }
                }

                if (information.getParameters().length != 0) {
                    out.println();
                    for (int i = 0; i < cmdPrefix.length(); i++) {
                        out.print(" ");
                    }
                }

                out.print(getDescription(information.getExecutorAnn().descriptionKey()));
                out.println();
            }
            out.println();
        }

        SubExecutorInformation[] subExecutorInformationArray = cInfo.getSubExecutorInformationSet().toArray(new SubExecutorInformation[0]);
        if (subExecutorInformationArray.length != 0) {
            Arrays.sort(subExecutorInformationArray);
            //打印子命令，如果有
            out.println(subCommandFlag);

            out.print(cmdPrefix);
            out.print(subExecutorInformationArray[0].getExecutorAnn().value());
            for (String alias : subExecutorInformationArray[0].getExecutorAnn().alias()) {
                out.print("/" + alias);
            }
            out.print(" ");

            for (Parameter parameter : subExecutorInformationArray[0].getParameters()) {
                if (parameter.isAnnotationPresent(ParaDsKey.class)) {
                    paraDsKey = parameter.getAnnotation(ParaDsKey.class).value();
                    out.print("[" + getDescription(paraDsKey) + "] ");
                } else {
                    out.print("[" + parameter.getName() + "] ");
                }
            }

            if (subExecutorInformationArray[0].getParameters().length != 0) {
                out.println();
                for (int i = 0; i < cmdPrefix.length(); i++) {
                    out.print(" ");
                }
            }

            out.print(getDescription(subExecutorInformationArray[0].getExecutorAnn().descriptionKey()));
            out.println();

            for (int i = 1; i < subExecutorInformationArray.length; i++) {
                for (int j = 0; j < cmdPrefix.length(); j++) {
                    out.print(" "); //对齐
                }

                out.print(subExecutorInformationArray[i].getExecutorAnn().value());
                for (String alias : subExecutorInformationArray[i].getExecutorAnn().alias()) {
                    out.print("/" + alias);
                }
                out.print(" ");

                for (Parameter parameter : subExecutorInformationArray[i].getParameters()) {
                    if (parameter.isAnnotationPresent(ParaDsKey.class)) {
                        paraDsKey = parameter.getAnnotation(ParaDsKey.class).value();
                        out.print("[" + getDescription(paraDsKey) + "] ");
                    } else {
                        out.print("[" + parameter.getName() + "] ");
                    }
                }

                if (subExecutorInformationArray[i].getParameters().length != 0) {
                    out.println();
                    for (int k = 0; k < cmdPrefix.length(); k++) {
                        out.print(" ");
                    }
                }

                out.print(getDescription(subExecutorInformationArray[i].getExecutorAnn().descriptionKey()));
                out.println();
            }
        }

        out.println();
    }

    @CommandExecutor(descriptionKey = "simpleterminal.defaults.help")
    public void help() {
        // 非常非常非常捡漏的i18n
        String getTargetHelp;
        if (Locale.getDefault().getLanguage().equals("zh")) {
            getTargetHelp = "键入 'help [命令名]' 以获取指定指令的详细信息。";
        } else {
            getTargetHelp = "Get details of the target command by entering 'help [command]'";
        }

        //打印所有命令
        //先获取所有命令中，最长的那个，用那个做间隔
        Set<String> allCommands = GlobalTransmitter.getCommandInformationMap().keySet();
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
                description = commandDescriptions.getProperty(dsKey, noAvailableDescription);
            }

            out.println(description);
        }

        out.println();
        out.println(getTargetHelp);
    }

    private boolean isUseChinese() {
        return Locale.getDefault().getLanguage().equals("zh");
    }

    private String getDescription(String dsKey) {
        String description;
        //先检查在default中有没有
        if (defaultDescriptions.containsKey(dsKey)) {
            description = defaultDescriptions.getProperty(dsKey);
        } else {
            description = commandDescriptions.getProperty(dsKey, noAvailableDescription);
        }
        return description;
    }
}
