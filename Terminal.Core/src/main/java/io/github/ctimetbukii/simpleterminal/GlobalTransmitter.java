package io.github.ctimetbukii.simpleterminal;

import io.github.ctimetbukii.simpleterminal.utils.records.CommandInformation;
import io.github.ctimetbukii.simpleterminal.utils.records.RunnableMethod;
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
    @Getter
    private static final HashMap<String, CommandInformation> commandInformationMap = new HashMap<>();
}
