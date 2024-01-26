package joserodpt.realscoreboard.managers;

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

import joserodpt.realscoreboard.RealScoreboard;
import joserodpt.realscoreboard.RealScoreboardPlugin;
import joserodpt.realscoreboard.api.managers.AbstractPlayerManager;
import joserodpt.realscoreboard.api.scoreboard.ScoreboardTask;
import joserodpt.realscoreboard.api.config.PlayerData;
import joserodpt.realscoreboard.api.config.RSBConfig;
import joserodpt.realscoreboard.api.utils.Text;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.HashMap;
import java.util.UUID;

public class PlayerManager extends AbstractPlayerManager implements Listener {

    private final RealScoreboard plugin;
    @Getter
    private final HashMap<UUID, ScoreboardTask> tasks = new HashMap<>();
    private final String[] vanishCommands = {"/pv", "/vanish", "/premiumvanish"};

    public PlayerManager(RealScoreboard plugin) {
        this.plugin = plugin;
    }

    @Override
    public void check(Player p) {
        if (RSBConfig.file().getList("Config.Bypass-Worlds").contains(p.getWorld().getName())) {
            if (this.tasks.containsKey(p.getUniqueId())) {
                this.tasks.get(p.getUniqueId()).cancel();
            }
            this.tasks.remove(p.getUniqueId());
        } else {
            if (RSBConfig.file().getBoolean("Config.RealScoreboard-Disabled-By-Default")) {
                PlayerData playerData = this.plugin.getDatabaseManager().getPlayerData(p.getUniqueId());
                playerData.setScoreboardON(false);
                this.plugin.getDatabaseManager().savePlayerData(playerData, true);
            }
            this.tasks.put(p.getUniqueId(), new ScoreboardTask(p, this.plugin));
            this.tasks.get(p.getUniqueId()).runTaskTimerAsynchronously(this.plugin.getPlugin(), RSBConfig.file().getInt("Config.Scoreboard-Refresh"), RSBConfig.file().getInt("Config.Scoreboard-Refresh"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void join(PlayerJoinEvent e) {
        check(e.getPlayer());
        if (e.getPlayer().isOp() && RealScoreboardPlugin.getNewUpdate()) {
            Text.send(e.getPlayer(), "&6&lWARNING &fThere is a new version of RealScoreboard! https://www.spigotmc.org/resources/realscoreboard-1-13-to-1-20-1.22928/");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void changeWorld(PlayerChangedWorldEvent e) {
        check(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        //check if this hiding behaviour is hidden
        if (RSBConfig.file().getBoolean("Config.Auto-Hide-In-Vanish")) {

            Player p = e.getPlayer();
            String cmd = e.getMessage().split(" ")[0];
            for (String vanishCommand : vanishCommands) {
                if (cmd.equalsIgnoreCase(vanishCommand)) {
                    PlayerData playerData = this.plugin.getDatabaseManager().getPlayerData(p.getUniqueId());
                    //before the command is processed, the vanish tag in the player is still the previous state, so we set it as the previous
                    playerData.setScoreboardON(isVanished(p));
                    this.plugin.getDatabaseManager().savePlayerData(playerData, true);
                    break;
                }
            }
        }
    }

    private boolean isVanished(Player player) {
        for (MetadataValue meta : player.getMetadata("vanished")) {
            if (meta.asBoolean()) return true;
        }
        return false;
    }
}