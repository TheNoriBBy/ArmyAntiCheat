package me.aviso.ArmyAntiCheat.Commands;

import me.aviso.ArmyAntiCheat.utils.Color;
import me.aviso.ArmyAntiCheat.Main;
import me.aviso.ArmyAntiCheat.Movement.PlayerMovementChecker;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReportCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (command.getName().equalsIgnoreCase("report")) {
            if (commandSender instanceof Player reporter) {
                if (strings.length != 1) {
                    reporter.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("report-usage", "&aUzycie:&f /report <player>")));
                    return true;
                }

                Player reportedPlayer = Bukkit.getPlayer(strings[0]);
                if (reportedPlayer == null) {
                    reporter.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("report-playernotfound", "&cNie ma takiego gracza.")));
                    return true;
                }

                if (Main.getInstance().reportingPlayers.getOrDefault(reportedPlayer, false)) {
                    reporter.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("report-message", "&cAntiCheat: &aGracz zostal zreportowany!")));
                    return true;
                }

                Main.getInstance().reportingPlayers.put(reportedPlayer, true);
                reporter.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("report-message", "&cAntiCheat: &aGracz zostal zreportowany!")));

                new PlayerMovementChecker(reportedPlayer, Main.getInstance().getConfig().getInt("report-checkingtime", 30)).runTaskTimer(Main.getInstance(), 0L, 1L);
            } else {
                commandSender.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("report-nonplayer", "&cTylko gracze moga uzywac tej komendy!")));
            }
            return true;
        }
        return false;
    }
}
