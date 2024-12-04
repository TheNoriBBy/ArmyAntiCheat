package me.aviso.ArmyAntiCheat;

public class CheatingNotification {
    private boolean isCheating;
    private String message;

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
