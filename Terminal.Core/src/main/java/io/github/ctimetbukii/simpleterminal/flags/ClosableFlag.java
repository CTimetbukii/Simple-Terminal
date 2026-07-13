package io.github.ctimetbukii.simpleterminal.flags;

/**
 * 标记一个指令处理器类需要有在关闭时处理的资源
 */
public interface ClosableFlag {
    /**
     * 关闭当前指令处理器中的资源。若没有注册自定义的stop命令，则默认的stop命令
     * 在执行时会依次调用各个指令处理器的closeResource方法。若该方法抛出了异常，则打印堆栈然后继续执行
     */
    @SuppressWarnings("unused") //这个方法没有显式调用，而是被反射调用了，所以IDE的unused警告不准
    void closeResources() throws Exception;
}
