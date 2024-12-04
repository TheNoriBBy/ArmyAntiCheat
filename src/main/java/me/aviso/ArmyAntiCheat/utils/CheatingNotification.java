package me.aviso.ArmyAntiCheat.utils;

public class CheatingNotification {
    private final boolean isCheating;
    private final String message;

    public CheatingNotification(boolean isCheating, String message) {
        this.isCheating = isCheating;
        this.message = message;
    }

    public boolean isCheating() {
        return isCheating;
    }

    public String getMessage() {
        return message;
    }
}