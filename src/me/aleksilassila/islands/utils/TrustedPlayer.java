package me.aleksilassila.islands.utils;

import com.sun.istack.internal.NotNull;

import java.util.UUID;

public class TrustedPlayer {
    private final UUID uuid;
    private boolean containerTrust;
    private boolean doorTrust;
    private boolean generalTrust;

    public TrustedPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    @NotNull
    public UUID getUUID() {
        return uuid;
    }

    public boolean isGenerallyTrusted() {
        return generalTrust;
    }

    public boolean isContainerTrusted() {
        return generalTrust || containerTrust;
    }

    public boolean isDoorTrusted() {
        return generalTrust || doorTrust;
    }

    public TrustedPlayer setGeneralTrust(boolean generalTrust) {
        this.generalTrust = generalTrust;
        return this;
    }

    public TrustedPlayer setContainerTrust(boolean containerTrust) {
        this.containerTrust = containerTrust;
        return this;
    }

    public TrustedPlayer setDoorTrust(boolean doorTrust) {
        this.doorTrust = doorTrust;
        return this;
    }
}
