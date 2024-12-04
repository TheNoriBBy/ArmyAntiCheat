package me.aviso.ArmyAntiCheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerMovementChecker extends BukkitRunnable {
    private final Player reportedPlayer;
    private final int checkingTime;
    private int seconds = 0;
    private final MovementPatternAnalyzer patternAnalyzer;

    public PlayerMovementChecker(Player reportedPlayer, int checkingTime) {
        this.reportedPlayer = reportedPlayer;
        this.checkingTime = checkingTime;
        this.patternAnalyzer = new MovementPatternAnalyzer();
    }

    @Override
    public void run() {

        UUID playerUUID = reportedPlayer.getUniqueId();

        if (seconds >= checkingTime * 20 || !Main.getInstance().isReportingPlayer(reportedPlayer)) {
            Main.getInstance().removeReportingPlayer(reportedPlayer);
            patternAnalyzer.analyzeMovements(playerUUID);
            cancel();
            return;
        }

        String movement = reportedPlayer.getLocation().getPitch() + "," + reportedPlayer.getLocation().getYaw();

        Main.getInstance().recordPlayerMovement(playerUUID, movement);

        //Bukkit.getLogger().info("(" + reportedPlayer.getName() + ") Movement: " + movement);
        Bukkit.getLogger().info(movement);

        seconds++;
    }
}