package com.example.netherdeathlimiter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * A simple Paper/Spigot plugin for Paper 1.21.10 that counts the number of times
 * a player dies in the Nether. Once a player dies twice in the Nether, they
 * are marked as banned from entering the Nether by setting a value on the
 * "NetherBanni" scoreboard objective. Command blocks or other datapacks can
 * then react to this scoreboard value (e.g. by repelling the player from
 * portals or teleporting them away). The plugin does not handle portal
 * repulsion or teleportation itself – it simply manages the death counter and
 * banned flag.
 */
public final class NetherDeathLimiter extends JavaPlugin implements Listener {

    // Scoreboard objectives for counting Nether deaths and tracking bans.
    private Objective deathsObjective;
    private Objective banObjective;

    @Override
    public void onEnable() {
        // Obtain the main scoreboard. This is persistent across server restarts.
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getMainScoreboard();

        // Ensure the death counter objective exists. It uses the "dummy"
        // criterion so we can increment it manually.
        deathsObjective = board.getObjective("MortsNether");
        if (deathsObjective == null) {
            deathsObjective = board.registerNewObjective(
                    "MortsNether",
                    "dummy",
                    ChatColor.RED + "Morts Nether");
            deathsObjective.setDisplayName(ChatColor.RED + "Morts Nether");
        }

        // Ensure the ban flag objective exists. Also a dummy criterion.
        banObjective = board.getObjective("NetherBanni");
        if (banObjective == null) {
            banObjective = board.registerNewObjective(
                    "NetherBanni",
                    "dummy",
                    ChatColor.DARK_RED + "Nether Banni");
            banObjective.setDisplayName(ChatColor.DARK_RED + "Nether Banni");
        }

        // Register this class as an event listener.
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("NetherDeathLimiter enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NetherDeathLimiter disabled.");
    }

    /**
     * Handles player death events. If a player dies in the Nether, their
     * MortsNether score is incremented. Once it reaches 2 or more the plugin
     * sets the NetherBanni score to 1. Command blocks or datapacks can
     * interpret this flag to lock the player out of the Nether.
     *
     * @param event the death event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        World world = player.getWorld();
        if (world.getEnvironment() == World.Environment.NETHER) {
            // Increment the player's Nether death counter.
            Score deathScore = deathsObjective.getScore(player.getName());
            int current = deathScore.getScore();
            deathScore.setScore(current + 1);

            // Check if the player has died twice or more in the Nether.
            if (current + 1 >= 2) {
                Score banScore = banObjective.getScore(player.getName());
                if (banScore.getScore() < 1) {
                    // Set their ban flag to 1. Do not decrease if already banned.
                    banScore.setScore(1);
                    player.sendMessage(ChatColor.DARK_RED + "Vous avez utilisé vos deux vies dans le Nether." +
                            ChatColor.GRAY + " Vous ne pouvez plus retourner dans le Nether.");
                }
            }
        }
    }
}
