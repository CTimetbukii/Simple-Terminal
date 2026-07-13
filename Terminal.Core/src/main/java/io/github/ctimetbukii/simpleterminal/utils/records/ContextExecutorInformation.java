package io.github.ctimetbukii.simpleterminal.utils.records;

import io.github.ctimetbukii.simpleterminal.annotations.ContextCommandExecutor;
import lombok.Getter;

import java.lang.reflect.Method;

public class ContextExecutorInformation {
    @Getter
    private ContextCommandExecutor executorAnn;
    @Getter
    private Class<?>[] paraTypes;

    public ContextExecutorInformation(Method method) {
        if (!method.isAnnotationPresent(ContextCommandExecutor.class)) {
            throw new IllegalArgumentException("No @ContextCommandExecutor for #" + method);
        }

        executorAnn = method.getAnnotation(ContextCommandExecutor.class);
        paraTypes = method.getParameterTypes();
    }
}
