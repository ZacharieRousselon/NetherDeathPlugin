package com.example.netherdeathlimiter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public final class NetherDeathLimiter extends JavaPlugin implements Listener {

    private Objective deathsObjective;
    private Objective banObjective;

    @Override
    public void onEnable() {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getMainScoreboard();

        // 1. Gestion de l'objectif de morts
        deathsObjective = board.getObjective("MortsNether");
        
        // Si l'objectif existe mais n'est pas "dummy", on le supprime pour le recréer proprement
        if (deathsObjective != null && !deathsObjective.getCriteria().equals("dummy")) {
            getLogger().warning("L'ancien objectif MortsNether n'était pas 'dummy'. Suppression et recréation.");
            deathsObjective.unregister();
            deathsObjective = null;
        }

        if (deathsObjective == null) {
            deathsObjective = board.registerNewObjective(
                    "MortsNether",
                    "dummy", 
                    ChatColor.RED + "Morts Nether");
        }

        // 2. Gestion de l'objectif de ban
        banObjective = board.getObjective("NetherBanni");
        if (banObjective == null) {
            banObjective = board.registerNewObjective(
                    "NetherBanni",
                    "dummy", 
                    ChatColor.DARK_RED + "Nether Banni");
        }

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("NetherDeathLimiter activé avec succès.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NetherDeathLimiter désactivé.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        // On récupère le monde via la location exacte du joueur au moment de la mort
        World world = player.getLocation().getWorld(); 

        // LOG DE DEBUG : Regardez votre console quand vous mourrez
        getLogger().info("Mort détectée pour " + player.getName() + " dans le monde : " + world.getName() + " (Environment: " + world.getEnvironment() + ")");

        // Vérification stricte : Est-ce le NETHER ?
        if (world.getEnvironment() == World.Environment.NETHER) {
            
            Score deathScore = deathsObjective.getScore(player.getName());
            int newScore = deathScore.getScore() + 1;
            
            // On applique le nouveau score
            deathScore.setScore(newScore);
            getLogger().info("Mort Nether validée. Nouveau score pour " + player.getName() + ": " + newScore);

            // Vérification du BAN (2 morts ou plus)
            if (newScore >= 2) {
                Score banScore = banObjective.getScore(player.getName());
                // On ne met le tag que si le joueur ne l'a pas déjà
                if (banScore.getScore() < 1) {
                    banScore.setScore(1);
                    player.sendMessage(ChatColor.DARK_RED + "ATTENTION : Vous avez atteint la limite de morts dans le Nether.");
                    player.sendMessage(ChatColor.GRAY + "Vous êtes désormais banni de cette dimension.");
                    getLogger().info("Joueur " + player.getName() + " banni du Nether (Tag NetherBanni mis à 1).");
                }
            }
        } else {
            getLogger().info("Mort ignorée par le compteur Nether (car nous sommes dans " + world.getEnvironment() + ")");
        }
    }
}
