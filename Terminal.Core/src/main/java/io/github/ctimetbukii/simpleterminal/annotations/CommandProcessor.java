package io.github.ctimetbukii.simpleterminal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个类为某个指令的处理器。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandProcessor {
    /**
     * 命令
     */
    String value();

    /**
     * 该命令的别称
     */
    String[] alias() default {};

    /**
     * 同时匹配大写指令，小写指令以及大小写混用指令。默认为true。 <br/>
     * 例如，该值为true时，mycommand，MYCOMMAND，MyCommand，mYCOMMand等都会被@CommandProcessor("mycommand")匹配。
     * 为false时则只有mycommand能被匹配
     */
    boolean matchUpperAndLowerCase() default true;

    /**
     * 当前命令是否含有上下文。默认为false即无上下文。<br/><br/>
     * 所谓上下文指令，即需要多次与控制台对话的指令，例如: <br/>
     * > send hello<br/>
     * To whom?<br/>
     * > CTimet bukii<br/>
     * send 'hello' to CTimet bukii done.<br/><br/>
     * 在这里，send指令就被视为一个上下文指令，第二次的输入CTimet bukii不被视为命令，而被视为send指令的上下文输入，
     * 传入被ContextCommandExecutor(stage = 1)标记的方法中。<br/>
     * 从技术上讲，即使不加hasContext属性，仍然可以通过判断是否存在含有SubCommandExecutor注解的方法来判断该命令是否存在上下文，
     * 但那样会引入额外的反射性能开支，而且，显式设置hasContext=true更能提醒开发者该命令存在上下文。因此引入hasContext()。
     * <br/>
     * 注意，Simple-Terminal仅根据此hasContext的值判断是否有上下文。若未显式设置此值为true，则即使类中有ContextCommandExecutor标记的
     * 方法，仍然视其为没有上下文。
     */
    boolean hasContext() default false;

    /**
     * 用于查询该命令描述的Properties Key。
     */
    String descriptionKey() default "";
}
