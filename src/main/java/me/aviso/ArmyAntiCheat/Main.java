package me.aviso.ArmyAntiCheat;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;

public class Main extends JavaPlugin {

    private static Main instance;

    private final Map<Player, Queue<String>> playerMovements = new HashMap<>();
    private final Map<Player, Boolean> reportingPlayers = new HashMap<>();
    private final Map<UUID, List<String>> playerMovementRecords = new HashMap<>();

    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        config = getConfig();
        loadMovements();
    }

    @Override
    public void onDisable() {
        playerMovements.clear();
        reportingPlayers.clear();
        playerMovementRecords.clear();
        saveMovements();
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

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command cmd, @NotNull String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("report")) {
            if (sender instanceof Player) {
                Player reporter = (Player) sender;
                if (args.length != 1) {
                    reporter.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("report-usage", "&aUzycie:&f /report <player>")));
                    return true;
                }

                Player reportedPlayer = Bukkit.getPlayer(args[0]);
                if (reportedPlayer == null) {
                    reporter.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("report-playernotfound", "&cNie ma takiego gracza.")));
                    return true;
                }

                if (reportingPlayers.getOrDefault(reportedPlayer, false)) {
                    reporter.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("report-message", "&cAntiCheat: &aGracz zostal zreportowany!")));
                    return true;
                }

                reportingPlayers.put(reportedPlayer, true);
                reporter.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("report-message", "&cAntiCheat: &aGracz zostal zreportowany!")));

                new PlayerMovementChecker(reportedPlayer, config.getInt("report-checkingtime", 30)).runTaskTimer(this, 0L, 1L);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("report-nonplayer", "&cTylko gracze moga uzywac tej komendy!")));
            }
            return true;
        } else if (cmd.getName().equalsIgnoreCase("record")) {
            if (sender instanceof Player) {
                Player recorder = (Player) sender;
                if (args.length != 1 || (!args[0].equalsIgnoreCase("real") && !args[0].equalsIgnoreCase("fake"))) {
                    recorder.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("record-usage", "&cUzycie: &f/record <real|fake>")));
                    return true;
                }

                String movementType = args[0].toLowerCase();
                new PlayerMovementRecorder(recorder, movementType, 30).runTaskTimer(this, 0L, 1L);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("record-nonplayer", "&cTylko gracze moga uzywac tej komendy!")));
            }
            return true;
        }
        return false;
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
                e.printStackTrace();
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
            e.printStackTrace();
        }
    }
}