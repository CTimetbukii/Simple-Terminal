package io.github.ctimetbukii.simpleterminal;

import io.github.ctimetbukii.simpleterminal.annotations.ContextCommandExecutor;
import io.github.ctimetbukii.simpleterminal.exceptions.IllegalJumpStageException;
import io.github.ctimetbukii.simpleterminal.exceptions.IllogicalPanicException;
import io.github.ctimetbukii.simpleterminal.process.CommandHandler;
import io.github.ctimetbukii.simpleterminal.process.ExecuteResult;
import io.github.ctimetbukii.simpleterminal.process.ExecuteState;
import lombok.Getter;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.Future;

public class Terminal {
    private final TerminalOptions options;
    @Getter
    private Thread asyncThread;

    @Getter
    private Future<?> monitorInputFuture;

    private boolean run = true;

    /*
     * 当前上下文中的指令。未输入带上下文的指令时，该值应当为null。上下文结束时，该值应重新赋值为null
     */
    private String contextCommand = null;

    /*
     * 当前上下文使用的输入次数。当contextCommand = null时，该值应当为-1。代表当下不存在上下文。
     * 若存在上下文，则此处的值应当与ContextCommandExecutor中标记的stage值一样。
     * 根据该值匹配stage
     */
    private int contextTimes = -1;

    //严格匹配的命令处理器。存放那些@CommandProcessor(matchUpperAndLowerCase=false)的命令处理器
    private final HashMap<String, CommandHandler> strictMatchCommandHandlers = new HashMap<>();
    //非严格匹配的命令处理器
    private final HashMap<String, CommandHandler> easeMatchCommandHandlers = new HashMap<>();
    //带有上下文的需要严格匹配的指令集
    private final HashSet<String> strictMatchCommandsWithContext = new HashSet<>();
    //带有上下文的非严格匹配指令集
    private final HashSet<String> easeMatchCommandsWithContext = new HashSet<>(); //这两个HashSet不能合并，否则匹配时可能导致由于要求严格匹配的指令被输入了含错误大小写的指令而错误地启用了上下文
    //上下文长度记录
    private final HashMap<String, Integer> contextLengthMap = new HashMap<>();

    public Terminal() {
        this(new TerminalOptions());
    }

    public Terminal(TerminalOptions options) {
        this.options = options;
    }

    /**
     * 启动终端监听
     */
    public void run() {
        //从锚定类路径中查找带有注解标记的方法/类
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(options.getAnchorPackagePath())
                .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false), new MethodAnnotationsScanner()));

        Set<Class<?>> commandProcessors = reflections.getTypesAnnotatedWith(CommandProcessor.class);

        //注册命令处理器
        commandProcessors.forEach(cls -> {
            CommandProcessor processorAnn = cls.getAnnotation(CommandProcessor.class);
            Method[] methods = cls.getMethods();
            int contextLength;

            if (processorAnn.hasContext()) {
                Optional<Method> maxContextExecutor =  Arrays.stream(methods).filter(method -> method.isAnnotationPresent(ContextCommandExecutor.class))
                        .max(Comparator.comparingInt(m -> m.getAnnotation(ContextCommandExecutor.class).stage()));
                if (maxContextExecutor.isPresent()) {
                    contextLength = maxContextExecutor.get().getAnnotation(ContextCommandExecutor.class).stage();
                } else {
                    throw new IllogicalPanicException("A CommandProcessor with context has not ContextCommandExecutor.");
                }
            } else {
                contextLength = 0;
            }

            contextLengthMap.put(processorAnn.value().toLowerCase(), contextLength);

            if (processorAnn.matchUpperAndLowerCase()) {
                easeMatchCommandHandlers.put(processorAnn.value().toLowerCase(), new CommandHandler(cls, contextLength, options.getOutputStream()));
            } else {
                strictMatchCommandHandlers.put(processorAnn.value(), new CommandHandler(cls, contextLength, options.getOutputStream()));
            }
            //处理含有上下文的指令
            if (processorAnn.hasContext()) {
                if (processorAnn.matchUpperAndLowerCase()) {
                    easeMatchCommandsWithContext.add(processorAnn.value());
                } else {
                    strictMatchCommandsWithContext.add(processorAnn.value());
                }
            }
        });

        if (options.isRunAsync()) {
            if (options.getRunExecutor() == null) {
                asyncThread = new Thread(this::monitorInput);
                asyncThread.start();
            } else {
                monitorInputFuture = options.getRunExecutor().submit(this::monitorInput);
            }
        } else {
            monitorInput();
        }
    }

    private void monitorInput() {
        try (Scanner scanner = new Scanner(options.getInputStream())){
            while (run && !Thread.currentThread().isInterrupted() && scanner.hasNextLine()) {
                String[] inputs = scanner.nextLine().split(" ");

                //检查当前是否存在上下文指令
                if (contextCommand != null) {
                    //存在上下文，检查当前已经使用的上下文输入次数
                    if (contextTimes != contextLengthMap.get(contextCommand)) {
                        //上下文次数没有达到记录的最大上下文次数。说明当前仍然存在于上下文环境中。
                        contextTimes ++;
                        //走上下文指令处理器

                        ExecuteResult result;

                        //检查命令是否存在
                        if (strictMatchCommandHandlers.containsKey(contextCommand)) {
                            result = strictMatchCommandHandlers.get(contextCommand).accept(inputs, contextTimes); //走上下文时，整个输入被传入当作参数
                        } else if (easeMatchCommandHandlers.containsKey(contextCommand.toLowerCase())) {
                            result = easeMatchCommandHandlers.get(contextCommand.toLowerCase()).accept(inputs, contextTimes);
                        } else {
                            //命令不存在
                            //上下文指令不应该不存在。此处出现无法调和的异常
                            throw new IllogicalPanicException("ContextCommand doesn't exists while 'contextCommand' field is NOT null!");
                        }

                        //检查执行结果。若执行出现参数问题，应当认为此次命令执行失败，清空上下文
                        if (result.getExecuteState() == ExecuteState.FAIL) {
                            contextTimes = -1;
                            contextCommand = null;
                        }

                        //如果需要重新执行
                        if (result.getExecuteState() == ExecuteState.NEED_REPEAT) {
                            //把contextTime重新减回来
                            contextTimes --;
                        }

                        if (result.getExecuteState() == ExecuteState.STAGE_JUMP_TO) {
                            //把contextTime跳转到指定的stage中，并检查该stage大于0
                            if (result.getJumToStage() > 0) {
                                contextTimes = result.getJumToStage() -1; // -1是因为在下一次while循环的开始又会对这个contextTime++，故减去1
                            } else {
                                //非法的跳跃stage，抛出异常
                                throw new IllegalJumpStageException("Illegal jump stage: " + result.getJumToStage());
                            }
                        }

                        continue; //当前的上下文指令已经处理完，走下一次while循环，避免跳出if块后继续执行剩下代码导致同一个命令被处理两次
                    } else {
                        //上下文次数已经达到记录的最大次数。证明此时已经完成上下文命令的上下文。我们清空上下文
                        contextTimes = -1;
                        contextCommand = null;
                        //跳出if块，让剩余代码继续正常执行。
                    }
                }

                //当前不存在上下文，正常执行
                //拿到顶部命令，如果有重复执行的指令则用重复执行的指令
                String topCommand = inputs[0];
                String[] args = Arrays.copyOfRange(inputs, 1, inputs.length);
                //若命令含有上下文，则设置好。该操作必须在命令执行之前，因为我们要用这个contextTimes去匹配stage
                if (strictMatchCommandsWithContext.contains(topCommand)
                    || easeMatchCommandsWithContext.contains(topCommand.toLowerCase())) {
                    contextCommand = topCommand;
                    contextTimes ++; //默认是-1，加1后为0，刚好匹配stage=0，代表初次指令输入
                }

                ExecuteResult result;

                //检查命令是否存在
                if (strictMatchCommandHandlers.containsKey(topCommand)) {
                    result = strictMatchCommandHandlers.get(topCommand).accept(args, contextTimes);
                } else if (easeMatchCommandHandlers.containsKey(topCommand.toLowerCase())) {
                    result = easeMatchCommandHandlers.get(topCommand.toLowerCase()).accept(args, contextTimes);
                } else {
                    //命令不存在
                    options.getCommandDoesntExistProcessor().accept(topCommand);
                    result = new ExecuteResult(ExecuteState.FAIL, -2);
                }

                if (result.getExecuteState() == ExecuteState.FAIL) {
                    contextTimes = -1;
                    contextCommand = null;
                }

                //普通命令或者上下文命令的stage0不做repeat处理和jumpTo处理
            }
        }
    }

    public void stop() {
        run = false;
        if (asyncThread != null) {
            asyncThread.interrupt();
        }
        if (monitorInputFuture != null) {
            monitorInputFuture.cancel(true);
        }
    }
}
