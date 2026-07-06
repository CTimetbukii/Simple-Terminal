package io.github.ctimetbukii.simpleterminal.annotations;

import java.lang.annotation.*;

/**
 * 标记具体执行子命令的方法，用法与CommandExecutor类似。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommandExecutor {
    /**
     * 子命令
     */
    String value();

    /**
     * 同时匹配大写指令和小写指令。默认为true。 <br/>
     * 例如，该值为true时，mycommand，MYCOMMAND，MyCommand，mYCOMMand等都会被@SubCommandExecutor("mycommand")匹配
     * 为false时则只有mycommand能被匹配
     */
    boolean matchUpperAndLowerCase() default true;

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
