package joserodpt.realscoreboard;

/*
 *   ____            _ ____                     _                         _
 *  |  _ \ ___  __ _| / ___|  ___ ___  _ __ ___| |__   ___   __ _ _ __ __| |
 *  | |_) / _ \/ _` | \___ \ / __/ _ \| '__/ _ \ '_ \ / _ \ / _` | '__/ _` |
 *  |  _ <  __/ (_| | |___) | (_| (_) | | |  __/ |_) | (_) | (_| | | | (_| |
 *  |_| \_\___|\__,_|_|____/ \___\___/|_|  \___|_.__/ \___/ \__,_|_|  \__,_|
 *
 *
 * Licensed under the MIT License
 * @author José Rodrigues
 * @link https://github.com/joserodpt/RealScoreboard
 */

import joserodpt.realscoreboard.api.RealScoreboardAPI;
import joserodpt.realscoreboard.api.config.RSBConfig;
import joserodpt.realscoreboard.api.scoreboard.RPlayerHook;
import joserodpt.realscoreboard.api.scoreboard.RScoreboard;
import joserodpt.realscoreboard.api.utils.Text;
import me.mattstudios.mf.annotations.Alias;
import me.mattstudios.mf.annotations.Command;
import me.mattstudios.mf.annotations.Completion;
import me.mattstudios.mf.annotations.Default;
import me.mattstudios.mf.annotations.Permission;
import me.mattstudios.mf.annotations.SubCommand;
import me.mattstudios.mf.base.CommandBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Command("realscoreboard")
@Alias({"rsb", "sb"})
public class RealScoreboardCommand extends CommandBase {

    private final String playerOnly = "Only players can use this command.";

    private final RealScoreboardAPI rsa;

    public RealScoreboardCommand(RealScoreboardAPI rsa) {
        this.rsa = rsa;
    }

    @Default
    public void defaultCommand(final CommandSender commandSender) {
        Text.send(commandSender,  "&fReal&dScoreboard &7| &fv" + this.rsa.getVersion());
    }

    @SubCommand("reload")
    @Permission("realscoreboard.admin")
    public void reloadCommand(final CommandSender commandSender) {
        this.rsa.reload();
        Text.send(commandSender, RSBConfig.file().getString("Config.Reloaded"));
    }

    @SubCommand("toggle")
    @Alias("t")
    @Permission("realscoreboard.toggle")
    public void toggleCommand(final CommandSender commandSender) {
        if (commandSender instanceof Player p) {
            RPlayerHook hook = rsa.getPlayerManager().getPlayerHook(p.getUniqueId());
            hook.setRealScoreboardVisible(!hook.isRealScoreboardVisible());
            Text.send(p, RSBConfig.file().getString("Config.Messages.Scoreboard-Toggle." + (hook.isRealScoreboardVisible() ? "ON" : "OFF")));
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("off")
    @Permission("realscoreboard.toggle")
    public void offCommand(final CommandSender commandSender) {
        if (commandSender instanceof Player p) {
            RPlayerHook hook = rsa.getPlayerManager().getPlayerHook(p.getUniqueId());
            hook.setRealScoreboardVisible(false);
            Text.send(p, RSBConfig.file().getString("Config.Messages.Scoreboard-Toggle.OFF"));
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("on")
    @Permission("realscoreboard.toggle")
    public void onCommand(final CommandSender commandSender) {
        if (commandSender instanceof Player p) {
            RPlayerHook hook = rsa.getPlayerManager().getPlayerHook(p.getUniqueId());
            hook.setRealScoreboardVisible(true);
            Text.send(p, RSBConfig.file().getString("Config.Messages.Scoreboard-Toggle.ON"));
        } else {
            Text.send(commandSender, playerOnly);
        }
    }

    @SubCommand("setscoreboard")
    @Alias("setsb")
    @Completion({"#scoreboards", "#players"})
    @Permission("realscoreboard.setscoreboard")
    public void setscoreboardcmd(final CommandSender commandSender, final String name, Player target) {
        RScoreboard sb = rsa.getScoreboardManager().getScoreboard(name);
        if (sb == null) {
            Text.send(commandSender, "Scoreboard not found with that name.");
            return;
        }

        if (target == null) {
            Text.send(commandSender, "Player not found.");
            return;
        }

        if (rsa.getPlayerManager().getPlayerHook(target.getUniqueId()).getScoreboard() == sb) {
            Text.send(commandSender, target.getName() + " &calready has that scoreboard applied.");
        } else {
            rsa.getPlayerManager().getPlayerHook(target.getUniqueId()).setScoreboard(sb);
            Text.send(commandSender, name + " scoreboard applied to " + target.getName());
        }
    }

    @SubCommand("debug")
    @Permission("realscoreboard.admin")
    public void debug(final CommandSender commandSender) {
        Text.send(commandSender, Arrays.asList("", "", Text.getPrefix(),
                "> &b&lPLUGIN info",
                "&fPlugin Version: &b" + this.rsa.getVersion(),
                "> &b&lSERVER info",
                "&fServer Name: &b" + Bukkit.getName(),
                "&fServer Version: &b" + Bukkit.getVersion(),
                "> &b&lHOST info",
                "&fJava Version: &b" + System.getProperty("java.version"),
                "&fOS Name: &b" + System.getProperty("os.name"),
                "&fOS Architecture: &b" + System.getProperty("os.arch"),
                "&fOS Version: &b" + System.getProperty("os.version"),
                "> &b&lDATABASE info",
                "&fDB Driver: &b" + RSBConfig.getSql().getString("driver"),
                "> &b&lSCOREBOARD info",
                "&fLoaded Scoreboards: &b" + this.rsa.getScoreboardManager().getScoreboards().size(),
                "> &b&lCONFIG info",
                "&fConfig Version: &b" + RSBConfig.file().getInt("Version"),
                "&e&lNOTE: &fThis information is intended to be shared with the developer in order to provide additional assistance."));
    }
}