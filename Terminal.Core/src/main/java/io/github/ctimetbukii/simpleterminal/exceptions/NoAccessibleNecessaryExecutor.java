package io.github.ctimetbukii.simpleterminal.exceptions;

/**
 * 当一个命令处理器中没有任何被CommandExecutor，SubCommandExecutor标记的方法时，抛出此异常
 */
public class NoAccessibleNecessaryExecutor extends RuntimeException {
    public NoAccessibleNecessaryExecutor(String message) {
        super(message);
    }
}
