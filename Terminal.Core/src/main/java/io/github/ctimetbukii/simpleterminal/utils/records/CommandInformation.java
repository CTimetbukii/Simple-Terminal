package io.github.ctimetbukii.simpleterminal.utils.records;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.ContextCommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.SubCommandExecutor;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class CommandInformation {
    @Getter
    private CommandProcessor processorAnn;

    @Getter
    private final Set<ExecutorInformation> executorInformationSet = new HashSet<>();

    @Getter
    private final Set<SubExecutorInformation> subExecutorInformationSet = new HashSet<>();

    public CommandInformation(Class<?> processorCls) {
        if (!processorCls.isAnnotationPresent(CommandProcessor.class)) {
            throw new IllegalArgumentException("No @CommandProcessor for " + processorCls);
        }

        processorAnn = processorCls.getAnnotation(CommandProcessor.class);

        for (Method method : processorCls.getMethods()) {
            if (method.isAnnotationPresent(CommandExecutor.class)) {
                executorInformationSet.add(new ExecutorInformation(method));
            } else if (method.isAnnotationPresent(SubCommandExecutor.class)) {
                subExecutorInformationSet.add(new SubExecutorInformation(method));
            }
        }
    }
}
