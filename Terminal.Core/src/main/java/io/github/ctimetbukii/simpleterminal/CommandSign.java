package io.github.ctimetbukii.simpleterminal;

import io.github.ctimetbukii.elegantconvertchecker.ConvertChecker;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 用于区分一个CommandExecutor标记的方法和另一个CommandExecutor标记的方法的Sign。
 */
public class CommandSign {
    @Getter
    private String commandValue;
    @Getter
    private String[] patterns;
    @Getter
    private Class<?>[] paraTypes;
    @Getter
    private int stage = 0;
    @Getter
    private boolean paraJoin;

    public CommandSign(String[] patterns, Class<?>[] paraTypes, int stage, boolean paraJoin) {
        this("", patterns, paraTypes, stage, paraJoin);
    }

    public CommandSign(String commandValue, String[] patterns, Class<?>[] paraTypes, int stage, boolean paraJoin) {
        this.commandValue = commandValue;
        this.patterns = patterns;
        this.paraTypes = paraTypes;
        this.stage = stage;
        this.paraJoin = paraJoin;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CommandSign) {
            CommandSign sign = (CommandSign) obj;
            return Objects.equals(sign.commandValue, this.commandValue)
                    && Arrays.equals(sign.patterns, this.patterns)
                    && Arrays.equals(sign.paraTypes, this.paraTypes)
                    && sign.stage == this.stage
                    && sign.paraJoin == this.paraJoin;
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(commandValue)
                + Arrays.hashCode(patterns)
                + Objects.hashCode(paraTypes)
                + this.stage
                + Objects.hashCode(paraJoin);
    }

    /**
     * 检查传入的参数是否满足此CommandSign中记录的types
     */
    public boolean isMatchTypes(String[] args) {
        //检查参数数量
        if (args.length != paraTypes.length) {
            //参数数量不相等。此时有种情况可以返回true，即标记了参数拼接的时候
            //检查是否标记了参数拼接
            if (this.isParaJoin()) {
                //标记了参数拼接。此时相当于把整个args拼接为一个str，然后来匹配参数类型
                //标记了参数拼接的情况下，paraTypes只能为{java.lang.String}，其他任何情况都是false
                return Arrays.equals(paraTypes, new Class[]{String.class});
            }

            //没启用参数拼接的情况下参数数量和paraTypes不相等，直接返回false就行

            return false;
        }

        //走到这个if分支代表args.length = signTypes.length
        //若参数数量都为0，则直接返回true
        if (args.length == 0) {
            return true;
        }

        //逐个校验参数
        for (int i = 0; i < args.length; i++) {
            if (!ConvertChecker.isConvertible(args[i], paraTypes[i])) {
                //若对应位置参数无法被转换，则返回false
                return false;
            }
        }

        //逐个校验成功，返回true
        return true;
    }

    /**
     * 检查传入的参数是否满足此CommandSign记录的patterns
     */
    public boolean isMatchPatterns(String[] args) {
        if (patterns.length == 0) { // patterns为空说明没有patterns要求
            return true;
        }

        if (args.length != patterns.length) { //有patterns要求
            //检查是否启用了参数拼接
            if (isParaJoin()) {
                //启用了参数拼接，此时patterns.length必须是1，若不是1，可以直接返回false
                if (patterns.length != 1) {
                    return false;
                }

                //确认有一个pattern，我们把args拼接为para，然后检查是否match
                String para = String.join(" ", args);
                return Pattern.matches(patterns[0], para);
            }
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            if (!Pattern.matches(patterns[i], args[i])) {
                return false;
            }
        }

        return true;
    }
}
