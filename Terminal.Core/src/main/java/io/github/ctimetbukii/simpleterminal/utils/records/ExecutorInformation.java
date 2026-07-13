package io.github.ctimetbukii.simpleterminal.utils.records;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import lombok.Getter;

import java.lang.reflect.Method;

public class ExecutorInformation {
    @Getter
    private CommandExecutor executorAnn;
    @Getter
    private Class<?>[] paraTypes;

    public ExecutorInformation(Method method) {
        if (!method.isAnnotationPresent(CommandExecutor.class)) {
            throw new IllegalArgumentException("No @CommandExecutor for #" + method);
        }

        executorAnn = method.getAnnotation(CommandExecutor.class);
        paraTypes = method.getParameterTypes();
    }
}
