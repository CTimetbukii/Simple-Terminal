package io.github.ctimetbukii.simpleterminal.utils.records;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class ExecutorInformation implements Comparable<ExecutorInformation> {
    @Getter
    private CommandExecutor executorAnn;
    @Getter
    private Parameter[] parameters;

    public ExecutorInformation(Method method) {
        if (!method.isAnnotationPresent(CommandExecutor.class)) {
            throw new IllegalArgumentException("No @CommandExecutor for #" + method);
        }

        executorAnn = method.getAnnotation(CommandExecutor.class);
        parameters = method.getParameters();
    }

    @Override
    public int compareTo(@NotNull ExecutorInformation o) {
        return parameters.length - o.parameters.length;
    }
}
