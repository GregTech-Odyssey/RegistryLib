package com.gto.registrylib.state;

import java.util.function.UnaryOperator;

public class StateDebugConfig {

    private boolean enabled;
    private boolean writable;
    private int readPermission = 2;
    private int writePermission = 4;

    public static StateDebugConfig disabled() {
        return new StateDebugConfig();
    }

    public static StateDebugConfig createEnabled() {
        return new StateDebugConfig().enabled(true);
    }

    public StateDebugConfig enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public StateDebugConfig writable(boolean writable) {
        this.writable = writable;
        return this;
    }

    public StateDebugConfig readPermission(int readPermission) {
        this.readPermission = readPermission;
        return this;
    }

    public StateDebugConfig writePermission(int writePermission) {
        this.writePermission = writePermission;
        return this;
    }

    public StateDebugConfig configure(UnaryOperator<StateDebugConfig> config) {
        return config.apply(this);
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean writable() {
        return writable;
    }

    public int readPermission() {
        return readPermission;
    }

    public int writePermission() {
        return writePermission;
    }
}
