package io.github.ctimetbukii.simpleterminal.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记一个方法为某个含上下文的指令的处理器
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextCommandExecutor {
    /**
     * 第几轮指令输入。0代表初次指令输入。从1开始递增代表上下文输入次数。例如: <br/> <br/>
     * >send 666  <br/>
     * To whom? <br/>
     * >CTimet bukii <br/>
     * Send '666' to CTimet bukii, done. <br/>
     *  <br/>
     *  在这个例子中。send 输入被stage=0的方法截获。而CTimet bukii输入则被stage=1的方法截获。
     *  CTimet bukii是在send这个含有上下文的指令中的第一次上下文输入 <br/>
     *  使用@CommandExecutor在命令调用时刻上等价于使用@ContextCommandExecutor(stage=0)，拿上面的例子来说，
     *  无论某个方法被@CommandExecutor标记还是被@ContextCommandExecutor(stage=0)标记，都会在send 666指令发出后
     *  被调用。区别是，若方法被@CommandExecutor标记，则传入的参数值会剪切掉send，只传入{"666"}作为args。
     *  若方法被@ContextCommandExecutor(stage=0)标记，则用户输入会被完整传入，即传入{"send","666"}作为args。
     *  <br/><br/>
     *  不要同时使用@CommandExecutor和@ContextCommandExecutor(stage=0)，这不会抛出异常，但可能导致一个方法被调用2次，
     *  且如上文所说，两次调用传入的args不一样，这会造成麻烦。
     */
    int stage();

    /**
     * 参见 {@link CommandExecutor#patterns()}
     */
    String[] patterns() default {};
}
