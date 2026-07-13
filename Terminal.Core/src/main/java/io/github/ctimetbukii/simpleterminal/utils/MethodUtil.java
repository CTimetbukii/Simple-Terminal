package io.github.ctimetbukii.simpleterminal.utils;

import io.github.ctimetbukii.simpleterminal.CommandSign;
import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.ContextCommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.SubcommandExecutor;

import java.lang.reflect.Method;

public class MethodUtil {
    public static CommandSign makeCommandExecutorCommandSign(Method method) {
        CommandExecutor executor = method.getAnnotation(CommandExecutor.class);
        return new CommandSign(executor.patterns(), method.getParameterTypes(), 0, executor.paraJoin(), executor.paraTransformMethod(), false);
    }

    public static CommandSign makeSubCommandExecutorCommandSign(Method method) {
        SubcommandExecutor executor = method.getAnnotation(SubcommandExecutor.class);
        return new CommandSign(executor.value(), executor.patterns(), method.getParameterTypes(), 0, executor.paraJoin(), executor.paraTransformMethod(), executor.matchAllCases());
    }

    public static CommandSign makeContextExecutorCommandSign(Method method) {
        ContextCommandExecutor executor = method.getAnnotation(ContextCommandExecutor.class);
        return new CommandSign(executor.patterns(), method.getParameterTypes(), executor.stage(), executor.paraJoin(), executor.paraTransformMethod(), false);
    }
}

