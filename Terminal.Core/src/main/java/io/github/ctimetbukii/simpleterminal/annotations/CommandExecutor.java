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

    /**
     * 是否启用参数拼接。所谓参数拼接即将参数与空格拼接起来作为一个参数传入方法。例如执行指令 cmd 1 2 3，
     * 启用参数拼接后，传入的参数将从 {"1","2","3"} 变为 {"1 2 3"}。当你认为用户输入的内容中需要包含空格时，启用参数拼接。
     * 默认为不启用。
     */
    boolean paraJoin() default false;
}
