package io.github.ctimetbukii.simpleterminal.utils;

import io.github.ctimetbukii.simpleterminal.CommandSign;
import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.ContextCommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.SubCommandExecutor;

import java.lang.reflect.Method;

public class MethodUtil {
    public static String[] getCommandExecutorPatterns(Method method) {
        return method.getAnnotation(CommandExecutor.class).patterns();
    }

    public static String[] getSubCommandExecutorPatterns(Method method) {
        return method.getAnnotation(SubCommandExecutor.class).patterns();
    }

    public static String[] getContextCommandExecutorPatterns(Method method) {
        return method.getAnnotation(ContextCommandExecutor.class).patterns();
    }

    public static CommandSign makeCommandExecutorCommandSign(Method method) {
        CommandExecutor executor = method.getAnnotation(CommandExecutor.class);
        return new CommandSign(executor.patterns(), method.getParameterTypes(), 0, executor.paraJoin());
    }

    public static CommandSign makeSubCommandExecutorCommandSign(Method method) {
        SubCommandExecutor executor = method.getAnnotation(SubCommandExecutor.class);
        return new CommandSign(executor.value(), executor.patterns(), method.getParameterTypes(), 0, executor.paraJoin());
    }

    public static CommandSign makeContextExecutorCommandSign(Method method) {
        ContextCommandExecutor executor = method.getAnnotation(ContextCommandExecutor.class);
        return new CommandSign(executor.patterns(), method.getParameterTypes(), executor.stage(), executor.paraJoin());
    }
}

