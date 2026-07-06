package io.github.ctimetbukii.simpleterminal.utils;

import io.github.ctimetbukii.elegantconvertchecker.Converter;

public class CastUtil {
    public static Object[] castArgs(String[] args, Class<?>[] types) {
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = Converter.convertTo(args[i], types[i]);
        }
        return result;
    }
}
