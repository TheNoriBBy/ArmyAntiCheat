package me.aviso.ArmyAntiCheat;

import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

public class MovementPatternAnalyzer {

    public void analyzeMovements(UUID playerUUID) {
        List<String> movements = Main.getInstance().getPlayerMovements(playerUUID);

        if (movements.isEmpty()) {
            return;
        }

        double[][] movementData = convertMovementsToData(movements);
        double[][] realMovement = convertMovementsToData(data.realmovements);
        double[][] fakeMovement = convertMovementsToData(data.fakemovements);

        boolean isSuspicious = detectAimAssist(movementData, realMovement, fakeMovement);

        if (isSuspicious) {
            notifyAdmins(playerUUID);
        }
    }

    private double[][] convertMovementsToData(List<String> movements) {
        double[][] data = new double[movements.size()][2];

        for (int i = 0; i < movements.size(); i++) {
            String[] parts = movements.get(i).split(",");
            data[i][0] = Double.parseDouble(parts[0]);
            data[i][1] = Double.parseDouble(parts[1]);
        }

        return data;
    }

    private boolean detectAimAssist(double[][] movementData, double[][] realMovement, double[][] fakeMovement) {
        double realSpeed = calcavgspeedbyplayer(realMovement);
        double fakeSpeed = calcavgspeedbyplayer(fakeMovement);
        double playerSpeed = calcavgspeedbyplayer(movementData);

        double speedThreshold = (fakeSpeed - realSpeed) * 0.8;

        if (playerSpeed > realSpeed + speedThreshold) {
            return true;
        }

        return isFejkMovement(movementData, fakeMovement);
    }

    private double calcavgspeedbyplayer(double[][] movements) {
        double totalSpeed = 0;
        int count = 0;

        for (int i = 1; i < movements.length; i++) {
            double deltaPitch = movements[i][0] - movements[i - 1][0];
            double deltaYaw = movements[i][1] - movements[i - 1][1];
            double speed = Math.sqrt(deltaPitch * deltaPitch + deltaYaw * deltaYaw);
            totalSpeed += speed;
            count++;
        }

        return count > 0 ? totalSpeed / count : 0;
    }

    private boolean isFejkMovement(double[][] movementData, double[][] fakeMovement) {
        double fakeAverageSpeed = calcavgspeedbyplayer(fakeMovement);

        for (int i = 1; i < movementData.length; i++) {
            double deltaPitch = Math.abs(movementData[i][0] - movementData[i - 1][0]);
            double deltaYaw = Math.abs(movementData[i][1] - movementData[i - 1][1]);

            double playerSpeed = Math.sqrt(deltaPitch * deltaPitch + deltaYaw * deltaYaw);

            if (playerSpeed > fakeAverageSpeed * 1.2) {
                return true;
            }
        }
        return false;
    }

    private void notifyAdmins(UUID playerUUID) {
        String playerName = Main.getInstance().getServer().getPlayer(playerUUID).getName();
        Bukkit.getLogger().info("AntiCheat: Wykryto podejrzany movement u gracza " + playerName);
    }
}