package me.aviso.ArmyAntiCheat.Movement;

import me.aviso.ArmyAntiCheat.utils.CheatingNotification;
import me.aviso.ArmyAntiCheat.utils.Color;
import me.aviso.ArmyAntiCheat.Main;
import me.aviso.ArmyAntiCheat.utils.data;

import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MovementPatternAnalyzer {
    private RandomForest classifier;
    private Instances datasetStructure;

    public MovementPatternAnalyzer() {
        initializeClassifier();
    }

    private void initializeClassifier() {
        try {
            ArrayList<Attribute> attributes = new ArrayList<>();
            attributes.add(new Attribute("avgSpeed"));
            attributes.add(new Attribute("maxSpeed"));
            attributes.add(new Attribute("avgDirectionChange"));
            attributes.add(new Attribute("maxDirectionChange"));

            ArrayList<String> classValues = new ArrayList<>();
            classValues.add("normal");
            classValues.add("cheating");
            attributes.add(new Attribute("class", classValues));

            datasetStructure = new Instances("MovementData", attributes, 0);
            datasetStructure.setClassIndex(datasetStructure.numAttributes() - 1);

            classifier = new RandomForest();

            Instances trainingData = new Instances(datasetStructure);

            for (String movement : data.realmovements) {
                double[] features = extractFeaturesFromMovement(movement);
                addTrainingInstance(trainingData, features, "normal");
            }

            for (String movement : data.fakemovements) {
                double[] features = extractFeaturesFromMovement(movement);
                addTrainingInstance(trainingData, features, "cheating");
            }

            if (trainingData.numInstances() > 0) {
                classifier.buildClassifier(trainingData);
                Main.logger.info("Classifier built successfully with {} instances", trainingData.numInstances());
            } else {
                Main.logger.warn("No training instances available. Classifier not built.");
            }
        } catch (Exception e) {
            Main.logger.error("Error initializing classifier: {}", e.getMessage(), e);
        }
    }

    private void addTrainingInstance(Instances data, double[] values, String classValue) {
        DenseInstance instance = new DenseInstance(1.0, values);
        instance.setDataset(data);
        instance.setClassValue(classValue);
        data.add(instance);
    }
    
    private double[] extractFeaturesFromMovement(String movement) {
        String[] parts = movement.split(",");
        double pitch = Double.parseDouble(parts[0]);
        double yaw = Double.parseDouble(parts[1]);

        double avgSpeed = Math.abs(pitch) + Math.abs(yaw);
        double maxSpeed = Math.max(Math.abs(pitch), Math.abs(yaw));
        double avgDirectionChange = Math.abs(pitch - yaw);
        double maxDirectionChange = Math.max(Math.abs(pitch), Math.abs(yaw));
    
        return new double[]{avgSpeed, maxSpeed, avgDirectionChange, maxDirectionChange};
    }

    private void addTrainingInstance(double[] values, String classValue) {
        DenseInstance instance = new DenseInstance(1.0, values);
        instance.setDataset(datasetStructure);
        instance.setClassValue(classValue);
        datasetStructure.add(instance);
    }

    public void analyzeMovements(UUID playerUUID) {
        List<String> movements = Main.getInstance().getPlayerMovements(playerUUID);

        if (movements.isEmpty()) {
            return;
        }

        double[][] movementData = convertMovementsToData(movements);
        CheatingNotification notification = detectAimAssist(movementData);

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

    private CheatingNotification detectAimAssist(double[][] movementData) {
        try {
            if (classifier == null) {
                Main.logger.error("Classifier is null. It may not have been properly initialized.");
                return new CheatingNotification(false, "Error: Classifier not initialized");
            }

            double[] features = extractFeatures(movementData);
            DenseInstance instance = new DenseInstance(1.0, features);
            instance.setDataset(datasetStructure);

            double[] distribution = classifier.distributionForInstance(instance);
            double cheatingProbability = distribution[datasetStructure.classAttribute().indexOfValue("cheating")];

            if (cheatingProbability > 0.5) {
                return new CheatingNotification(true, Color.reColor(Main.getInstance().getConfig().getString("MPA-Cheat-Notify", "Nienaturalne zachowanie wykryte u gracza (Prawdopodobienstwo: %probability%)")
                        .replace("%probability%", String.format("%.2f%%", cheatingProbability * 100))));
            } else {
                return new CheatingNotification(false, Color.reColor(Main.getInstance().getConfig().getString("MPA-NoCheat-Notify", "Podczas sprawdzania gracza nie znaleziono podejrzanych zachowan.")));
            }
        } catch (Exception e) {
            Main.logger.error("Error in detectAimAssist: " + e.getMessage(), e);
            return new CheatingNotification(false, "Error occurred during analysis");
        }
    }

    private double[] extractFeatures(double[][] movementData) {
        double avgSpeed = calcavgspeedbyplayer(movementData);
        double maxSpeed = calculateMaxSpeed(movementData);
        double avgDirectionChange = calculateAverageDirectionChange(movementData);
        double maxDirectionChange = calculateMaxDirectionChange(movementData);

        return new double[]{avgSpeed, maxSpeed, avgDirectionChange, maxDirectionChange};
    }

    private double calculateMaxSpeed(double[][] movements) {
        double maxSpeed = 0;
        for (int i = 1; i < movements.length; i++) {
            double deltaPitch = movements[i][0] - movements[i - 1][0];
            double deltaYaw = movements[i][1] - movements[i - 1][1];
            double speed = Math.sqrt(deltaPitch * deltaPitch + deltaYaw * deltaYaw);
            maxSpeed = Math.max(maxSpeed, speed);
        }
        return maxSpeed;
    }

    private double calculateAverageDirectionChange(double[][] movements) {
        double totalDirectionChange = 0;
        int count = 0;
        for (int i = 2; i < movements.length; i++) {
            double prevDeltaPitch = movements[i-1][0] - movements[i-2][0];
            double prevDeltaYaw = movements[i-1][1] - movements[i-2][1];
            double currDeltaPitch = movements[i][0] - movements[i-1][0];
            double currDeltaYaw = movements[i][1] - movements[i-1][1];
            double directionChange = Math.abs(Math.atan2(currDeltaYaw, currDeltaPitch) - Math.atan2(prevDeltaYaw, prevDeltaPitch));
            totalDirectionChange += directionChange;
            count++;
        }
        return count > 0 ? totalDirectionChange / count : 0;
    }

    private double calculateMaxDirectionChange(double[][] movements) {
        double maxDirectionChange = 0;
        for (int i = 2; i < movements.length; i++) {
            double prevDeltaPitch = movements[i-1][0] - movements[i-2][0];
            double prevDeltaYaw = movements[i-1][1] - movements[i-2][1];
            double currDeltaPitch = movements[i][0] - movements[i-1][0];
            double currDeltaYaw = movements[i][1] - movements[i-1][1];
            double directionChange = Math.abs(Math.atan2(currDeltaYaw, currDeltaPitch) - Math.atan2(prevDeltaYaw, prevDeltaPitch));
            maxDirectionChange = Math.max(maxDirectionChange, directionChange);
        }
        return maxDirectionChange;
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
}