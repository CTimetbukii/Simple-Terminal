package io.github.ctimetbukii.simpleterminal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当传入的命令参数不符合要求时，走该注解标记的方法。该注解标记的方法可以无参也可以有参。有参时，参数只能为1个，且必须是String[]
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgsIllegalExecutor {
}
