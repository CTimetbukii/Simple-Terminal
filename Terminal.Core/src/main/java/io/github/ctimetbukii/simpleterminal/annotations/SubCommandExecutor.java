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
}
