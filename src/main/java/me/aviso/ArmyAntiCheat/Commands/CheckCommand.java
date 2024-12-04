package me.aviso.ArmyAntiCheat.Commands;

import me.aviso.ArmyAntiCheat.utils.Color;
import me.aviso.ArmyAntiCheat.Main;
import me.aviso.ArmyAntiCheat.ReportSystem.ReportSystem;
import me.aviso.ArmyAntiCheat.ReportSystem.PlayerReportData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.UUID;

public class CheckCommand implements CommandExecutor {
    private final ReportSystem reportSystem;

    public CheckCommand(ReportSystem reportSystem) {
        this.reportSystem = reportSystem;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (command.getName().equalsIgnoreCase("check")) {
            if (commandSender instanceof Player checker) {
                Player targetPlayer = Bukkit.getPlayer(strings[0]);
                if (strings.length != 1 && targetPlayer == null) {
                    UUID targetUUID = targetPlayer.getUniqueId();
                    PlayerReportData reportData = reportSystem.getReportData(targetUUID);

                    if (reportData != null) {
                        commandSender.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("check-nodata", "&cDane dla tego gracza nie zostaly znalezione")));
                        return true;
                    }

                    commandSender.sendMessage("Report Data for " + targetPlayer.getName() + ":");
                    commandSender.sendMessage("UUID: " + targetUUID);
                    commandSender.sendMessage("Times reported by anticheat: " + reportData.getAnticheatReports());
                    commandSender.sendMessage("Last report by anticheat: " + (reportData.getLastAnticheatReport() > 0 ? new Date(reportData.getLastAnticheatReport()).toString() : "Never"));
                    commandSender.sendMessage("Times reported by players: " + reportData.getPlayerReports());
                } else {
                    commandSender.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("check-usage", "&cUzycie: /check <player>")));
                    return true;
                }
            } else {
                commandSender.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("check-nonplayer", "&cTylko gracze moga uzywac tej komendy!")));
                return true;
            }
        }

        return true;
    }
}