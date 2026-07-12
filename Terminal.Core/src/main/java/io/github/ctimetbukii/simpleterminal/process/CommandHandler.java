package io.github.ctimetbukii.simpleterminal.process;

import io.github.ctimetbukii.elegantconvertchecker.ConvertChecker;
import io.github.ctimetbukii.elegantconvertchecker.Converter;
import io.github.ctimetbukii.simpleterminal.CommandSign;
import io.github.ctimetbukii.simpleterminal.TerminalOptions;
import io.github.ctimetbukii.simpleterminal.annotations.*;
import io.github.ctimetbukii.simpleterminal.exceptions.IllegalInjectObjectException;
import io.github.ctimetbukii.simpleterminal.exceptions.NeedRepeatException;
import io.github.ctimetbukii.simpleterminal.exceptions.RepeatCommandSignException;
import io.github.ctimetbukii.simpleterminal.flags.ClosableFlag;
import io.github.ctimetbukii.simpleterminal.utils.CastUtil;
import io.github.ctimetbukii.simpleterminal.utils.GlobalTransmitter;
import io.github.ctimetbukii.simpleterminal.utils.RunnableMethod;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.ctimetbukii.simpleterminal.utils.MethodUtil.*;

public class CommandHandler {
    //命令与Method的映射
    private final HashMap<CommandSign, Method> commandExecutors = new HashMap<>();

    //子命令与Method的映射
    private final HashMap<CommandSign, Method> subCommandExecutors = new HashMap<>();

    //上下文命令与Method的映射
    private final HashMap<CommandSign, Method> contextCommandExecutors = new HashMap<>();

    //当传入的参数不符合任何CommandSign时，走此method
    private Method argsIllegalExecutor;

    //上下文长度
    @Getter
    private final int contextLength;

    private final Object invokeObj;

    private final TerminalOptions options;

    /**
     * 为某个指令处理类构造一个CommandHandler中间层。若该类不处理上下文指令，则contextLength必须传入0。
     * 若有上下文指令，则传入该类中ContextCommandExecutor标记的最大stage。
     *
     * @param cls           指令处理类
     * @param contextLength 类中的最大stage。若类不处理上下文指令，必须传入0
     * @param options        TerminalOption
     */
    @SneakyThrows
    public CommandHandler(Class<?> cls, int contextLength, TerminalOptions options) {
        Method[] methods = cls.getMethods();
        invokeObj = cls.newInstance();

        this.contextLength = contextLength;
        this.options = options;


        //检查是否有需要inject的PrintStream
        for (Field field : cls.getFields()) {
            injectFields(field, invokeObj);
        }
        for (Field field : cls.getDeclaredFields()) {
            injectFields(field, invokeObj);
        }

        CommandSign sign;
        for (Method method : methods) {
            method.setAccessible(true); //跳过安全检查，访问private的同时，使invoke速度加快

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
                continue;
            }

            if (method.isAnnotationPresent(ContextCommandExecutor.class)) {
                sign = makeContextExecutorCommandSign(method);
                if (contextCommandExecutors.containsKey(sign)) {
                    throw new RepeatCommandSignException(cls);
                }
                contextCommandExecutors.put(sign, method);
                continue;
            }

            if (method.isAnnotationPresent(ArgsIllegalExecutor.class)) {
                argsIllegalExecutor = method;
            }
        }

        //检查是否标记了ClosableFlag
        if (ClosableFlag.class.isAssignableFrom(cls)) {
            //若标记，则将这个closeResource方法，连同invokeObj，打包送到那个stop指令那里
            RunnableMethod runnableMethod = new RunnableMethod(cls.getMethod("closeResources"), invokeObj);
            GlobalTransmitter.getCloseResourceMethods().add(runnableMethod);
        }
    }

    @SneakyThrows
    private void injectFields(Field field, Object invokeObj) {
        if (field.isAnnotationPresent(InjectOutPrintStream.class)) {
            //检查field类型是不是PrintStream及它的子类
            if (PrintStream.class.isAssignableFrom(field.getType())) {
                //注入TerminalOption中的输出流
                field.setAccessible(true);
                field.set(invokeObj, this.options.getOutputStream());
            } else {
                //不是你还标记这个注解？抛出异常
                throw new IllegalInjectObjectException("Field " + field.getName() + "from Class " + invokeObj.getClass().getName() + " cannot be annotated with InjectOutPrintStream!");
            }
        }

        if (field.isAnnotationPresent(InjectErrPrintStream.class)) {
            //检查
            if (PrintStream.class.isAssignableFrom(field.getType())) {
                //注入
                field.setAccessible(true);
                field.set(invokeObj, this.options.getErrOutputStream());
            } else {
                throw new IllegalInjectObjectException("Field " + field.getName() + "from Class " + invokeObj.getClass().getName() + " cannot be annotated with InjectErrPrintStream!");
            }
        }

        if (field.isAnnotationPresent(InjectTerminalOption.class)) {
            if (TerminalOptions.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                field.set(invokeObj, this.options);
            } else {
                throw new IllegalInjectObjectException("Field " + field.getName() + "from Class " + invokeObj.getClass().getName() + " cannot be annotated with InjectTerminalOption!");
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
    @SneakyThrows
    public ExecuteResult accept(String[] args, int contextStage) {
        Set<CommandSign> signs;
        HashMap<CommandSign, Method> executorMap;
        boolean isSubCommandRegistered = false;

        if (contextLength != 0 && contextStage != 0) { //contextStage = 0时，走正常的commandExecutor
            signs = contextCommandExecutors.keySet().stream()
                    .filter(cSign -> cSign.getStage() == contextStage)
                    .filter(cSign -> cSign.isMatchTypes(args))
                    .filter(cSign -> cSign.isMatchPatterns(args))
                    .collect(Collectors.toSet());
            executorMap = contextCommandExecutors;
        } else if ((!subCommandExecutors.isEmpty()) && args.length != 0) { //说明存在子命令，且输入中有能够被视为子命令的东西，检查子命令匹配
            //子命令
            String subCommand = args[0];
            //检查该子命令是否被注册
            isSubCommandRegistered = subCommandExecutors.keySet().stream().anyMatch(cSign -> cSign.getCommandValue().equals(subCommand));
            if (isSubCommandRegistered) {
                //已经被注册，走子命令通道
                //截断args
                String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                signs = subCommandExecutors.keySet().stream()
                        .filter(cSign -> cSign.getCommandValue().equals(subCommand))
                        .filter(cSign -> cSign.isMatchTypes(subArgs))
                        .filter(cSign -> cSign.isMatchPatterns(subArgs))
                        .collect(Collectors.toSet());
                executorMap = subCommandExecutors;
            } else {
                //没有被注册相关的子命令。这个时候，走普通的commandExecutor通道
                signs = commandExecutors.keySet().stream()
                        .filter(cSign -> cSign.isMatchTypes(args))
                        .filter(cSign -> cSign.isMatchPatterns(args))
                        .collect(Collectors.toSet());
                executorMap = commandExecutors;
            }
        } else {
            signs = commandExecutors.keySet().stream()
                    .filter(cSign -> cSign.isMatchTypes(args))
                    .filter(cSign -> cSign.isMatchPatterns(args))
                    .collect(Collectors.toSet());
            executorMap = commandExecutors;
            //这两块代码重复了。我知道这很丑，但是没关系，它能跑
        }

        //检查是否为空
        if (signs.isEmpty()) {
            //为空说明没有符合条件的CommandExecutor，传入 参数错误 命令处理器
            if (argsIllegalExecutor != null) {
                //检查参数错误命令处理器中有没有参数
                if (argsIllegalExecutor.getParameterCount() == 0) {
                    //没有参数，就正常执行，不传参了
                    argsIllegalExecutor.invoke(invokeObj);
                } else {
                    //有参数。传入一整个数组参数。参数错误命令处理器要求如果有参数，则必须是一个数组
                    argsIllegalExecutor.invoke(invokeObj, (Object[]) args);
                }
            } else {
                //参数错误命令处理器此时未定义，执行默认行为
                options.getErrOutputStream().println("Command args are illegal!");
            }
            return new ExecuteResult(ExecuteState.FAIL, -2);
        } else {
            //不为空，说明匹配到了对应的CommandSign.
            //这里的值只可能是一个，因为我们早做了CommandSign去重
            CommandSign sign = signs.iterator().next();

            try {
                Object returned = null;
                //检查是否要求进行参数拼接
                if (sign.isParaJoin()) {
                    //进行参数拼接后传入
                    String para;
                    //取出对应的Method，执行
                    if (isSubCommandRegistered) {
                        para = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    } else {
                        para = String.join(" ", args);
                    }
                    executorMap.get(sign).invoke(invokeObj, para);
                } else {
                    //不要求参数拼接
                    if (isSubCommandRegistered) {
                        returned = executorMap.get(sign).invoke(invokeObj, CastUtil.castArgs(Arrays.copyOfRange(args, 1, args.length), sign.getParaTypes(), sign.getTransformExecutor()));
                    } else {
                        returned = executorMap.get(sign).invoke(invokeObj, CastUtil.castArgs(args, sign.getParaTypes(), sign.getTransformExecutor()));
                    }
                }

                if (returned != null && ConvertChecker.isCoercibleToInt(returned.toString())) {
                    return new ExecuteResult(ExecuteState.STAGE_JUMP_TO, Converter.convertToInt(returned.toString()));
                }
            } catch (InvocationTargetException e) {
                if (e.getCause() != null && e.getCause() instanceof NeedRepeatException){
                    return new ExecuteResult(ExecuteState.NEED_REPEAT, -2);
                }
                //noinspection CallToPrintStackTrace
                e.printStackTrace();
                return new ExecuteResult(ExecuteState.FAIL, -2);
            }
        }
        return new ExecuteResult(ExecuteState.SUCCEED, -2);
    }
}
