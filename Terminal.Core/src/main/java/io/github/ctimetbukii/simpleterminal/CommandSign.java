package io.github.ctimetbukii.simpleterminal;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 用于区分一个CommandExecutor标记的方法和另一个CommandExecutor标记的方法的Sign。
 */
public class CommandSign {
    @Getter
    private String commandValue;
    @Getter
    private String[] patterns;
    @Getter
    private Class<?>[] types;
    @Getter
    private int stage = 0;

    public CommandSign(String[] patterns, Class<?>[] types, int stage) {
        this("", patterns, types, stage);
    }

    public CommandSign(String commandValue, String[] patterns, Class<?>[] types, int stage) {
        this.commandValue = commandValue;
        this.patterns = patterns;
        this.types = types;
        this.stage = stage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CommandSign) {
            CommandSign sign = (CommandSign) obj;
            return Objects.equals(sign.commandValue, this.commandValue)
                    && Arrays.equals(sign.patterns, this.patterns)
                    && Arrays.equals(sign.types, this.types)
                    && sign.stage == this.stage;
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(commandValue)
                + Arrays.hashCode(patterns)
                + Arrays.hashCode(types)
                + this.stage;
    }
}
