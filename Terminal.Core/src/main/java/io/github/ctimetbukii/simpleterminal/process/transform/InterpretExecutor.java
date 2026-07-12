package io.github.ctimetbukii.simpleterminal.process.transform;

import io.github.ctimetbukii.elegantconvertchecker.ConvertChecker;
import io.github.ctimetbukii.elegantconvertchecker.Converter;

public class InterpretExecutor implements TransformExecutor {
    @Override
    public boolean checkTransformable(String source, Class<?> targetType) {
        return ConvertChecker.isInterpretable(source, targetType);
    }

    @Override
    public <T> T transformTo(String source, Class<T> targetType) {
        return Converter.interpretTo(source, targetType);
    }
}
