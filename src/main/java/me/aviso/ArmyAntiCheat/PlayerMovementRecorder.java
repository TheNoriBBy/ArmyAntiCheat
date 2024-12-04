package me.aviso.ArmyAntiCheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerMovementRecorder extends BukkitRunnable {
    private final Player recorder;
    private final String movementType;
    private final int duration;
    private int seconds = 0;

    public PlayerMovementRecorder(Player recorder, String movementType, int duration) {
        this.recorder = recorder;
        this.movementType = movementType;
        this.duration = duration;
    }

    @Override
    public void run() {
        UUID playerUUID = recorder.getUniqueId();

        if (seconds >= duration * 20) {
            cancel();
            return;
        }

        String movement = recorder.getLocation().getPitch() + "," + recorder.getLocation().getYaw();

        if (movementType.equals("real")) {
            data.realmovements.add(movement);
        } else {
            data.fakemovements.add(movement);
        }

        Bukkit.getLogger().info("Recorded " + movementType + " movement: " + movement);
        seconds++;
    }
}