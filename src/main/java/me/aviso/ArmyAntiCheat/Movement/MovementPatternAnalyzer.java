package me.aviso.ArmyAntiCheat.Movement;

import me.aviso.ArmyAntiCheat.utils.CheatingNotification;
import me.aviso.ArmyAntiCheat.utils.Color;
import me.aviso.ArmyAntiCheat.Main;
import me.aviso.ArmyAntiCheat.utils.data;

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

        CheatingNotification notification = detectAimAssist(movementData, realMovement, fakeMovement);

        if (notification.isCheating()) {
            Main.logger.info("Cheating detected: {}", notification.getMessage());
        } else {
            Main.logger.info("No cheating detected: {}", notification.getMessage());
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

    private CheatingNotification detectAimAssist(double[][] movementData, double[][] realMovement, double[][] fakeMovement) {
        double realSpeed = calcavgspeedbyplayer(realMovement);
        double fakeSpeed = calcavgspeedbyplayer(fakeMovement);
        double playerSpeed = calcavgspeedbyplayer(movementData);

        double speedThreshold = (fakeSpeed - realSpeed) * 0.8;

        //if (playerSpeed > realSpeed + speedThreshold) {
        //    return new CheatingNotification(true, "Detected high speed: " + playerSpeed + " exceeds real speed: " + realSpeed);
        //}

        CheatingNotification patternCheck = analyzeMovementPatterns(movementData, realMovement, fakeMovement);
        if (patternCheck.isCheating()) {
            return patternCheck;
        }

        return new CheatingNotification(false, Color.reColor(Main.getInstance().getConfig().getString("MPA-NoCheat-Notify", "Podczas sprawdzania gracza nie znaleziono podejrzanych zachowan.")));
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

    private CheatingNotification analyzeMovementPatterns(double[][] playerMovement, double[][] realMovement, double[][] fakeMovement) {
        double realAverageSpeed = calcavgspeedbyplayer(realMovement);
        double fakeAverageSpeed = calcavgspeedbyplayer(fakeMovement);
        double playerAverageSpeed = calcavgspeedbyplayer(playerMovement);

        double speedThreshold = Math.abs(fakeAverageSpeed - realAverageSpeed) * 0.8;
        double directionChangeThreshold = 15.0;

        for (int i = 1; i < playerMovement.length; i++) {
            double playerDeltaPitch = Math.abs(playerMovement[i][0] - playerMovement[i - 1][0]);
            double playerDeltaYaw = Math.abs(playerMovement[i][1] - playerMovement[i - 1][1]);
            double playerSpeed = Math.sqrt(playerDeltaPitch * playerDeltaPitch + playerDeltaYaw * playerDeltaYaw);

            //if (playerSpeed > realAverageSpeed + speedThreshold || playerSpeed > fakeAverageSpeed + speedThreshold) {
            //    return new CheatingNotification(true, "Detected high speed: " + playerSpeed + " exceeds both real: " + realAverageSpeed + " and fake: " + fakeAverageSpeed);
            //}

            if (playerDeltaPitch > directionChangeThreshold || playerDeltaYaw > directionChangeThreshold) {
                double realDeltaPitch = Math.abs(realMovement[i][0] - realMovement[i - 1][0]);
                double realDeltaYaw = Math.abs(realMovement[i][1] - realMovement[i - 1][1]);

                if (playerDeltaPitch > realDeltaPitch * 1.5 || playerDeltaYaw > realDeltaYaw * 1.5) {
                    return new CheatingNotification(true, Color.reColor(Main.getInstance().getConfig().getString("MPA-Cheat-Notify","Nienaturalne zachowanie wykryte u gracza (%playerDeltaPitch%, %playerDeltaYaw%)").replace("%playerDeltaPitch%", String.valueOf(playerDeltaPitch)).replace("%playerDeltaYaw%", String.valueOf(playerDeltaYaw))));
                }
            }
        }
        return new CheatingNotification(false, Color.reColor(Main.getInstance().getConfig().getString("MPA-NoCheat-Notify", "Podczas sprawdzania gracza nie znaleziono podejrzanych zachowan.")));
    }
}