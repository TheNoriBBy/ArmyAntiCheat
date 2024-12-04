package me.aviso.ArmyAntiCheat;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

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
    }

    @Override
    public void onDisable() {
        playerMovements.clear();
        reportingPlayers.clear();
        playerMovementRecords.clear();
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
        }
        return false;
    }
}