package io.github.ctimetbukii.simpleterminal.utils;

import io.github.ctimetbukii.simpleterminal.utils.transform.TransformExecutor;

public class CastUtil {
    public static Object[] castArgs(String[] args, Class<?>[] types, TransformExecutor transformExecutor) {
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = transformExecutor.transformTo(args[i], types[i]);
        }
        return result;
    }
}
