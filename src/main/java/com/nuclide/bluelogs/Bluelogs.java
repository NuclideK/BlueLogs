package com.nuclide.bluelogs;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.IOException;
import java.util.Objects;


public final class Bluelogs extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();

    public FileConfiguration getConfigFile() {
        return config;
    }

    @Override
    public void onEnable() {
        config.options().copyDefaults(true);
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
        log("Started up version 1.2.0", false);
        log("Started up version 1.2.0", true);
    }
    @Override
    public void onDisable() {
        log("Shutting down.", false);
        log("Shutting down.", true);
    }

    public void discordLog(String message, String url) {
        Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
            @Override
            public void run() {
                DiscordWebHook webHook = new DiscordWebHook(url);
                webHook.addEmbed(new DiscordWebHook.EmbedObject().setDescription(message));
                try {
                    webHook.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void log(String message, boolean staff) {
        if (config.getBoolean("logConsole")) {
            getLogger().info(message);
        }
        if (!Objects.equals(config.getString("all_logs_webhook"), "disabled") && !staff) {
            discordLog(message, config.getString("all_logs_webhook"));
        }
        if (!Objects.equals(config.getString("staff_logs_webhook"), "disabled") && staff) {
            discordLog(message, config.getString("staff_logs_webhook"));
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        switch (e.getInventory().getType()) {
            case CHEST:
            case BARREL:
            case SHULKER_BOX:
                if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
                    log(e.getPlayer().getName() + " IN CREATIVE MODE opened " + e.getInventory().getType() + " at " + e.getInventory().getLocation().getWorld().getName() + ". XYZ: " + e.getInventory().getLocation().getBlockX() + " " + e.getInventory().getLocation().getBlockY() + " " + e.getInventory().getLocation().getBlockZ(), true);
                } else {
                    log(e.getPlayer().getName() + " opened " + e.getInventory().getType() + " in " + e.getInventory().getLocation().getWorld().getName() + ". XYZ: " + e.getInventory().getLocation().getBlockX() + " " + e.getInventory().getLocation().getBlockY() + " " + e.getInventory().getLocation().getBlockZ(), false);
                }
            default:
                break;
        }
    }


    @EventHandler
    public void onPlaceEvent(EntityPlaceEvent e) {
        if (e.getEntityType() == EntityType.ENDER_CRYSTAL) {
            if (config.getBoolean("blockCrystal")) {
                if (!e.getEntity().getLocation().getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                    log("End crystal action cancelled at " + e.getEntity().getLocation().toVector(), false);
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onFlint(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getItem() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getClickedBlock().getType().isBurnable() || e.getClickedBlock().getType() == Material.TNT) {
                    switch (e.getItem().getType()) {
                        case FLINT_AND_STEEL:
                        case FIRE_CHARGE:
                            log(e.getPlayer().getName() + " used fire on " + e.getClickedBlock().getType() + " in " + e.getClickedBlock().getWorld().getName() + ". XYZ: " + e.getClickedBlock().getLocation().getBlockX() + " " + e.getClickedBlock().getLocation().getBlockY() + " " + +e.getClickedBlock().getLocation().getBlockZ(), true);
                            if (config.getBoolean("blockTnt")) {
                                if (e.getClickedBlock().getType() == Material.TNT) {
                                    e.setCancelled(true);
                                    log(e.getPlayer().getName() + "'s TNT event was cancelled, fear not!", false);
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
        }
    }


    @EventHandler
    public void onPlayerGamemodeChange(PlayerGameModeChangeEvent e) {
        log(e.getPlayer().getName() + " changed their gamemode to " + e.getNewGameMode(), true);
    }

    @EventHandler
    public void onItemDropped(PlayerDropItemEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            log(e.getPlayer().getName() + " dropped item " + e.getItemDrop().getItemStack().getType() + " (" + e.getItemDrop().getItemStack().getAmount() + "x)" + " while in CREATIVE MODE", true);
        }
    }
}