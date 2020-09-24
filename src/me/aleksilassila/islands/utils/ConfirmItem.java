package me.aleksilassila.islands.utils;

import java.util.Date;

public class ConfirmItem {
    public String command;
    public long time;
    private final long validForInMs;

    public ConfirmItem(String command, long validForInMs) {
        this.command = command;
        this.validForInMs = validForInMs;
        this.time = new Date().getTime();
    }

    public boolean expired() {
        return new Date().getTime() - time >= validForInMs;
    }
}
