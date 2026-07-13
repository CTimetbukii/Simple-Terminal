package io.github.ctimetbukii.simpleterminal.utils.records;

import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.Method;

public class RunnableMethod implements Runnable {
    @Getter
    private Method method;
    @Getter
    private Object invokeObj;

    public RunnableMethod(Method method, Object invokeObj) {
        this.method = method;
        this.invokeObj = invokeObj;
    }

    @Override
    @SneakyThrows
    public void run() {
        method.invoke(invokeObj);
    }
}
