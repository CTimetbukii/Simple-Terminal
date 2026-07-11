package io.github.ctimetbukii.simpleterminal.utils.transform;

public interface TransformExecutor {
    boolean checkTransformable(String source, Class<?> targetType);

    <T> T transformTo(String source, Class<T> targetType);
}
