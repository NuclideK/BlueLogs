package com.nuclide.bluelogs;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;
import java.util.Objects;

/*
Well, Uh I'm sorry you had to find this project, but I will try to make it better over time, any suggestions / tips / features you can make a request for

This is pretty much garbage code (but hey it works), but it's my first plugin really, and made to learn java & spigot basics. Just a little project ya know ;)
 */
public final class Bluelogs extends JavaPlugin implements Listener {
    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        config.options().copyDefaults(true);
        saveConfig();
        getServer().getPluginManager().registerEvents(this, this);
        log("Started up version " + this.getDescription().getVersion(), true);
    }
    @Override
    public void onDisable() {
        writeFile("Shutting down.");
        getLogger().info("Bluelogs was shut down properly");
    }
    public void discordLog(String message, String url,String username) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            // Make webhook stuff
            DiscordWebHook webHook = new DiscordWebHook(url);
            webHook.setUsername(username + " " + this.getDescription().getVersion());
            webHook.setAvatarUrl("https://upload.wikimedia.org/wikipedia/commons/thumb/f/ff/Solid_blue.svg/225px-Solid_blue.svg.png");
            // set webhook message
            webHook.setContent(message);
            // hopefully send webhook
            try {
                webHook.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    public void writeFile(String message) {
        File file = new File(this.getDataFolder(),java.time.LocalDateTime.now().getDayOfMonth() +"."+ java.time.LocalDateTime.now().getMonthValue()+"."+java.time.LocalDateTime.now().getYear() + "-event-logs.txt");
        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if (file.exists()) {
            try {
                Writer output;
                output = new BufferedWriter(new FileWriter(file.getAbsoluteFile(), true));
                // Pretend you don't see the line below me, thanks!
                output.write(System.lineSeparator() + "[" + java.time.LocalTime.now().getHour() + ":" + java.time.LocalTime.now().getMinute() + "] " + message);
                output.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            getLogger().severe("No logging file?!");
        }
    }

    public void log(String message, boolean staff) {
        if (config.getBoolean("logConsole")) {
            getLogger().info(message);
        }
        if (!Objects.equals(config.getString("all_logs_webhook"), "disabled") && !staff) {
            discordLog(message, config.getString("all_logs_webhook"),"Bluelogs");
        }
        if (!Objects.equals(config.getString("staff_logs_webhook"), "disabled") && staff) {
            discordLog(message, config.getString("staff_logs_webhook"),"[STAFF] Bluelogs");
        }
        if (config.getBoolean("logFile")) {
            writeFile(message);
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
        if (e.getItem() != null && e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getClickedBlock().getType().isBurnable() || e.getClickedBlock().getType() == Material.TNT) {
                    switch (e.getItem().getType()) {
                        case FLINT_AND_STEEL:
                        case FIRE_CHARGE:
                            log(e.getPlayer().getName() + " used fire on " + e.getClickedBlock().getType() + " in " + e.getClickedBlock().getWorld().getName() + ". XYZ: " + e.getClickedBlock().getLocation().getBlockX() + " " + e.getClickedBlock().getLocation().getBlockY() + " " + e.getClickedBlock().getLocation().getBlockZ(), true);
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