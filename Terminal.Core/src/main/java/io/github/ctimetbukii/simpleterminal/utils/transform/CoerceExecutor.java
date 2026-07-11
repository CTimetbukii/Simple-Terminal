package io.github.ctimetbukii.simpleterminal.utils.transform;

import io.github.ctimetbukii.elegantconvertchecker.ConvertChecker;
import io.github.ctimetbukii.elegantconvertchecker.Converter;

public class CoerceExecutor implements TransformExecutor {
    @Override
    public boolean checkTransformable(String source, Class<?> targetType) {
        return ConvertChecker.isCoercible(source, targetType);
    }

    @Override
    public <T> T transformTo(String source, Class<T> targetType) {
        return Converter.coerceTo(source, targetType);
    }
}
