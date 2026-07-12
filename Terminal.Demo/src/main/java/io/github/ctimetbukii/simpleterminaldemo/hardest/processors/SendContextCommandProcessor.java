package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.annotations.*;
import io.github.ctimetbukii.simpleterminal.process.transform.ArgsTypeTransformMethod;

import java.io.PrintStream;


@CommandProcessor(value = "send", matchUpperAndLowerCase = false, hasContext = true)
public class SendContextCommandProcessor {
    @InjectOutPrintStream
    private PrintStream out;

    @CommandExecutor
    public void send(String content) {
        out.println("Send '" + content + "' to whom?");
    }

    @ContextCommandExecutor(stage = 1, paraJoin = true) //paraJoin =true处理用户名中包含空格的情况
    public void acceptUser(String user) {
        out.println("Send to '" + user + "' done.");
        out.println("Your feeling rate: ");
    }

    @ContextCommandExecutor(stage = 2)
    public void acceptUserFeelingRate(int rate) {
        out.println("Rate = " + rate);
        out.println("Your feeling float rate:");
    }

    @ContextCommandExecutor(stage = 3, paraTransformMethod = ArgsTypeTransformMethod.CONVERT) //在该模式下，输入1.111111111111111等超出float精度的数据会提示参数不合法
    public void acceptUserFeelingFloatRate(float rate) {
        out.println("F Rate = " + rate);
        out.println("Your feeling float rate 2:");
    }

    @ContextCommandExecutor(stage = 4) //这一轮对话中输入1.1111111111111111就不会提示参数不合法，而是传入float=1.1112，这是强制转换的结果
    public void acceptUserFeelingFloatRate2(float rate) {
        out.println("F Rate = " + rate);
    }

    @ArgsIllegalExecutor
    public void fail() {
        out.println("参数不合法");
    }
}
