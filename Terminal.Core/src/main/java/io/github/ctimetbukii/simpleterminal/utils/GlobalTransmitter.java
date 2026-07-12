package io.github.ctimetbukii.simpleterminal.utils;

import lombok.Getter;

import java.util.HashMap;
import java.util.HashSet;

/**
 * 用于在指令处理类之间传递一些内容
 */
public class GlobalTransmitter {
    @Getter
    private static final HashMap<String, Object> transmitter = new HashMap<>();
    @Getter
    private static final HashSet<RunnableMethod> closeResourceMethods = new HashSet<>();
}
