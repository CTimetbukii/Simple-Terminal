package io.github.ctimetbukii.simpleterminal.utils.records;

import io.github.ctimetbukii.simpleterminal.annotations.SubcommandExecutor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class SubExecutorInformation implements Comparable<SubExecutorInformation> {
    @Getter
    private SubcommandExecutor executorAnn;
    @Getter
    private Parameter[] parameters;

    public SubExecutorInformation(Method method) {
        if (!method.isAnnotationPresent(SubcommandExecutor.class)) {
            throw new IllegalArgumentException("No @SubCommandExecutor for #" + method);
        }

        executorAnn = method.getAnnotation(SubcommandExecutor.class);
        parameters = method.getParameters();
    }

    @Override
    public int compareTo(@NotNull SubExecutorInformation o) {
        if (!this.executorAnn.value().equals(o.executorAnn.value())) {
            return this.executorAnn.value().compareTo(o.executorAnn.value());
        } else {
            return this.parameters.length - o.executorAnn.patterns().length;
        }
    }
}
