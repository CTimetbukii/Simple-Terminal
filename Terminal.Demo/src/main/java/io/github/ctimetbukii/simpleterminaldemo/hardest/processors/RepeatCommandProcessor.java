package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.ContextCommandExecutor;
import io.github.ctimetbukii.simpleterminal.exceptions.NeedRepeatException;

@CommandProcessor(value = "repeat", hasContext = true)
public class RepeatCommandProcessor {
    @CommandExecutor
    public void repeat(String arg) {
        System.out.println("arg = " + arg);
        if ("true".equals(arg)) {
            System.out.println("Repeat?");
        }
    }

    @ContextCommandExecutor(stage = 1)
    public void repeat1(boolean repeat) {
        if (repeat) {
            System.out.println("Repeated. Again?");
            throw new NeedRepeatException();
        }
        System.out.println("Stop. Jump to which stage OR try fail repeat by entering true/false");
    }

    @ContextCommandExecutor(stage = 2)
    public void failedRepeat2(boolean repeat) {
        if (repeat) {
            System.out.println("Failed repeat example, throw not NeedRepeatException");
            throw new RuntimeException(); // 控制台会报错，相当于命令执行失败。再次键入时不会再走这个方法了
        }
        System.out.println("Meow@");
    }

    @ContextCommandExecutor(stage = 2, patterns = {"^[1-4]\\d*$"}) //patterns限制了jumpTo只能为1-4的正整数
    public int jumpTo(int jumpTo) {
        System.out.println("Jump to " + jumpTo + ".");
        System.out.println("Type stage " +jumpTo + "'s args:");
        return jumpTo;
    }

    @ContextCommandExecutor(stage = 3)
    public void stage3(String c) {
        System.out.println("Stage 3");
    }

    @ContextCommandExecutor(stage = 4)
    public void stage4(String c) {
        System.out.println("Stage 4");
    }
}
