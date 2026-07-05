package io.github.ctimetbukii.simpleterminal;

public enum ArgsTypeTransformMethod {
    /**
     * <p>Convert strictly. All args have to obey the standard form of its type. For example:</p>
     * <br/>
     * <p>"true"->Boolean.class √ </p>
     * <p>"True"->Boolean.class ×</p>
     * <p>"12.23"->Float.class √</p>
     * <p>"12.23333"->Float.class ×</p>
     */
    CONVERT,
    /**
     * Coerce to the target type.
     */
    COERCE,
    /**
     *
     */
    INTERPRET
}
