package io.github.ctimetbukii.simpleterminal.utils.records;

import io.github.ctimetbukii.simpleterminal.annotations.SubCommandExecutor;
import lombok.Getter;

import java.lang.reflect.Method;

public class SubExecutorInformation {
    @Getter
    private SubCommandExecutor executorAnn;
    @Getter
    private Class<?>[] paraTypes;

    public SubExecutorInformation(Method method) {
        if (!method.isAnnotationPresent(SubCommandExecutor.class)) {
            throw new IllegalArgumentException("No @SubCommandExecutor for #" + method);
        }

        executorAnn = method.getAnnotation(SubCommandExecutor.class);
        paraTypes = method.getParameterTypes();
    }
}
