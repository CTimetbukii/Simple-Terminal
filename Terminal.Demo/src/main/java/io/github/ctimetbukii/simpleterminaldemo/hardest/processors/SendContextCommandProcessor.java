package io.github.ctimetbukii.simpleterminaldemo.hardest.processors;

import io.github.ctimetbukii.simpleterminal.ArgsTypeTransformMethod;
import io.github.ctimetbukii.simpleterminal.annotations.ArgsIllegalExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandExecutor;
import io.github.ctimetbukii.simpleterminal.annotations.CommandProcessor;
import io.github.ctimetbukii.simpleterminal.annotations.ContextCommandExecutor;



@CommandProcessor(value = "send", matchUpperAndLowerCase = false, hasContext = true)
public class SendContextCommandProcessor {
    @CommandExecutor
    public void send(String content) {
        System.out.println("Send '" + content + "' to whom?");
    }

    /*
     *
     */
    @ContextCommandExecutor(stage = 1, paraJoin = true) //paraJoin =true处理用户名中包含空格的情况
    public void acceptUser(String user) {
        System.out.println("Send to '" + user + "' done.");
        System.out.println("Your feeling rate: ");
    }

    @ContextCommandExecutor(stage = 2)
    public void acceptUserFeelingRate(int rate) {
        System.out.println("Rate = " + rate);
        System.out.println("Your feeling float rate:");
    }

    @ContextCommandExecutor(stage = 3, paraTransformMethod = ArgsTypeTransformMethod.CONVERT) //在该模式下，输入1.111111111111111等超出float精度的数据会提示参数不合法
    public void acceptUserFeelingFloatRate(float rate) {
        System.out.println("F Rate = " + rate);
        System.out.println("Your feeling float rate 2:");
    }

    @ContextCommandExecutor(stage = 4) //这一轮对话中输入1.1111111111111111就不会提示参数不合法，而是传入float=1.1112，这是强制转换的结果
    public void acceptUserFeelingFloatRate2(float rate) {
        System.out.println("F Rate = " + rate);
    }

    @ArgsIllegalExecutor
    public void fail() {
        System.out.println("参数不合法");
    }
}
