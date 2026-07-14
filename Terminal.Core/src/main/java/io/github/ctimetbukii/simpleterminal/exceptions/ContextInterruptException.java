package io.github.ctimetbukii.simpleterminal.exceptions;

/**
 * 当不希望上下文继续进行时，抛出此异常
 */
public class ContextInterruptException extends RuntimeException {
    public ContextInterruptException() {
        super();
    }
}
