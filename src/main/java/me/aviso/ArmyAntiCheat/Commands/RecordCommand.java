package me.aviso.ArmyAntiCheat.Commands;

import me.aviso.ArmyAntiCheat.utils.Color;
import me.aviso.ArmyAntiCheat.Main;
import me.aviso.ArmyAntiCheat.Movement.PlayerMovementRecorder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RecordCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (command.getName().equalsIgnoreCase("record")) {
            if (commandSender instanceof Player recorder) {
                if (strings.length != 1 || (!strings[0].equalsIgnoreCase("real") && !strings[0].equalsIgnoreCase("fake"))) {
                    recorder.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("record-usage", "&cUzycie: &f/record <real|fake>")));
                    return true;
                }

                String movementType = strings[0].toLowerCase();
                new PlayerMovementRecorder(recorder, movementType, 30).runTaskTimer(Main.getInstance(), 0L, 1L);
            } else {
                commandSender.sendMessage(Color.reColor(Main.getInstance().getConfig().getString("record-nonplayer", "&cTylko gracze moga uzywac tej komendy!")));
            }
            return true;
        }
        return false;
    }
}
