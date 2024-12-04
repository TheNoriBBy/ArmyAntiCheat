package me.aviso.ArmyAntiCheat;

import me.aviso.ArmyAntiCheat.Commands.CheckCommand;
import me.aviso.ArmyAntiCheat.Commands.RecordCommand;
import me.aviso.ArmyAntiCheat.Commands.ReportCommand;
import me.aviso.ArmyAntiCheat.ReportSystem.ReportSystem;
import me.aviso.ArmyAntiCheat.utils.data;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;

public class Main extends JavaPlugin {

    private static Main instance;
    private ReportSystem reportSystem;

    public static Logger logger = LoggerFactory.getLogger(Main.class);

    private final Map<Player, Queue<String>> playerMovements = new HashMap<>();
    public final Map<Player, Boolean> reportingPlayers = new HashMap<>();
    private final Map<UUID, List<String>> playerMovementRecords = new HashMap<>();

    public FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        config = getConfig();
        registerCommand();
        loadMovements();
        reportSystem = new ReportSystem();

        //Bukkit.getLogger().info(String.valueOf(data.realmovements));
    }

    @Override
    public void onDisable() {
        playerMovements.clear();
        reportingPlayers.clear();
        playerMovementRecords.clear();
        saveMovements();
    }

    public void registerCommand() {
        Objects.requireNonNull(this.getCommand("Report")).setExecutor(new ReportCommand());
        Objects.requireNonNull(this.getCommand("Record")).setExecutor(new RecordCommand());
        Objects.requireNonNull(this.getCommand("Check")).setExecutor(new CheckCommand(reportSystem));
    }

    public static Main getInstance() {
        return instance;
    }

    public boolean isReportingPlayer(Player player) {
        return reportingPlayers.getOrDefault(player, false);
    }

    public void removeReportingPlayer(Player player) {
        reportingPlayers.remove(player);
    }

    public void recordPlayerMovement(UUID playerUUID, String movement) {
        playerMovementRecords.computeIfAbsent(playerUUID, k -> new ArrayList<>()).add(movement);
    }

    public List<String> getPlayerMovements(UUID playerUUID) {
        return playerMovementRecords.getOrDefault(playerUUID, Collections.emptyList());
    }

    private void loadMovements() {
        loadMovementsFromFile(data.realmovements, "realmovements.RAAC");
        loadMovementsFromFile(data.fakemovements, "fakemovements.RAAC");
    }

    private void loadMovementsFromFile(List<String> movements, String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    movements.add(line);
                }
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    private void saveMovements() {
        saveMovementsToFile(data.realmovements, "realmovements.RAAC");
        saveMovementsToFile(data.fakemovements, "fakemovements.RAAC");
    }

    private void saveMovementsToFile(List<String> movements, String fileName) {
        File file = new File(getDataFolder(), fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (String movement : movements) {
                writer.write(movement);
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}