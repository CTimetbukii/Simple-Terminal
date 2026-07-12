package io.github.ctimetbukii.simpleterminal.exceptions;

/**
 * 当注入的对象类型与期望不符时，抛出此异常
 */
public class IllegalInjectObjectException extends RuntimeException {
    public IllegalInjectObjectException(String message) {
        super(message);
    }
}
