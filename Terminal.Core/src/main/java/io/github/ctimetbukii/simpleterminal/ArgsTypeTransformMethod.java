package io.github.ctimetbukii.simpleterminal;

import io.github.ctimetbukii.elegantconvertchecker.Converter;
import io.github.ctimetbukii.elegantconvertchecker.ConvertChecker;

/**
 * 从用户输入的字符串，转换到方法要求的参数类型，需要的方法转换方式
 */
public enum ArgsTypeTransformMethod {
    /**
     * <p>严格转换到对应类型。 所有的参数都必须符合对应的参数形式。
     * 关于详细的转换规则, 见:{@link Converter#convertTo(String, Class)}</p>
     * @see ConvertChecker#isConvertible(String, Class)
     */
    CONVERT,
    /**
     * 强制转换到对应类型。关于转换的详细规则, 见: {@link Converter#coerceTo(String, Class)}
     * @see ConvertChecker#isCoercible(String, Class)
     */
    COERCE,
    /**
     * 解释到对应类型。关于详细的转换规则, 见:
     * {@link Converter#interpretTo(String, Class)}
     * @see ConvertChecker#isInterpretable(String, Class)
     */
    INTERPRET,
    /**
     * 先尝试解释到对应类型，失败后尝试强制转换到对应类型。
     * 关于详细的转换规则，见：{@link Converter#coerceOrInterpretTo(String, Class)}
     * @see ConvertChecker#isCoercibleWithInterpretable(String, Class)
     */
    COERCE_WITH_INTERPRET
}
