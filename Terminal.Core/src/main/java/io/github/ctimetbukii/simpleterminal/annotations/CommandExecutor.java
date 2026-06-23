package io.github.ctimetbukii.simpleterminal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记具体执行命令的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandExecutor {
    /**
     * 方法参数应当遵循的格式要求，使用正则表达式编写，依次按位置表示每个位置的参数格式。默认为空，即不作格式要求
     */
    String[] patterns() default {};
}
