package io.github.ctimetbukii.simpleterminal.process;

import lombok.Getter;

public class ExecuteResult {
    @Getter
    private final ExecuteState executeState;
    @Getter
    private final int jumToStage;

    public ExecuteResult(ExecuteState executeState, int jumpToStage) {
        this.executeState = executeState;
        this.jumToStage = jumpToStage;
    }
}
