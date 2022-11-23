package net.azisaba.azisabaachievements.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum Side {
    PROXY,
    SERVER,
    ;

    @Contract(pure = true)
    @NotNull
    public Side getOpposite() {
        switch (this) {
            case PROXY:
                return SERVER;
            case SERVER:
                return PROXY;
            default:
                throw new IllegalStateException("Unknown side: " + this);
        }
    }
}
