package io.github.ctimetbukii.simpleterminal.process;

import io.github.ctimetbukii.simpleterminal.CommandSign;
import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.SubCommandExecutor;
import io.github.ctimetbukii.simpleterminal.exceptions.RepeatCommandSignException;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.*;

import static io.github.ctimetbukii.simpleterminal.utils.MethodUtil.makeCommandExecutorCommandSign;
import static io.github.ctimetbukii.simpleterminal.utils.MethodUtil.makeSubCommandExecutorCommandSign;

public class CommandHandler implements ICommandProcessor {
    //命令与Method的映射
    private final HashMap<CommandSign, Method> commandExecutors = new HashMap<>();

    //子命令与Method的映射
    private final HashMap<CommandSign, Method> subCommandExecutors = new HashMap<>();

    //上下文长度
    @Getter
    private final int contextLength;

    public CommandHandler(Class<?> cls, int contextLength) {
        Method[] methods = cls.getMethods();

        this.contextLength = contextLength;

        CommandSign sign;
        for (Method method : methods) {
            if (method.isAnnotationPresent(CommandExecutor.class)) {
                sign = makeCommandExecutorCommandSign(method);
                //检查是否有同样的patterns
                if (commandExecutors.containsKey(sign)) {
                    throw new RepeatCommandSignException(cls);
                }
                //sign唯一，存入map
                commandExecutors.put(sign, method);
                continue;
            }

            if (method.isAnnotationPresent(SubCommandExecutor.class)) {
                sign = makeSubCommandExecutorCommandSign(method);
                if (subCommandExecutors.containsKey(sign)) {
                    throw new RepeatCommandSignException(cls);
                }
                subCommandExecutors.put(sign, method);
            }
        }
    }

    /**
     * 处理命令
     *
     * @param args         传入的命令参数，若当前指令是含上下文的命令，则保护用户的全部输入，若不包含上下文，则不包含顶部命令。 <br/>
     *                     例如，执行 mycommand 1 2 3，若该指令hasContext=true，则此处传入{"mycommand","1","2","3"}。
     *                     若该指令hasContext=false，则此处传入{"1","2","3"} 。 <br/>
     *                     此时子命令被视为命令参数。
     * @param contextStage 上下文阶段。对于没有上下文的指令，该值应填0
     */
    @Override
    public void accept(String[] args, int contextStage) {
        //检查上下文阶段是否符合要求
        System.out.println(Arrays.toString(args));
        System.out.println(contextStage);
    }
}
