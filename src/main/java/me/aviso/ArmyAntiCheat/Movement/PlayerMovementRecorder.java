package me.aviso.ArmyAntiCheat.Movement;

import me.aviso.ArmyAntiCheat.Main;
import me.aviso.ArmyAntiCheat.utils.data;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
        if (seconds >= duration * 20) {
            cancel();
            return;
        }

        String movement = recorder.getLocation().getPitch() + "," + recorder.getLocation().getYaw();

        if (movementType.equals("real")) {
            data.realmovements.add(movement);
        } else if (movementType.equals("fake")) {
            data.fakemovements.add(movement);
        } else {
            cancel();
        }

        Main.logger.info("Recorded {} movement: {}", movementType, movement);
        seconds++;
    }
}