package ro.marioenache.customitemchecker.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {

    private final JavaPlugin plugin;
    private boolean notifyPlayers;
    private String notificationMessage;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        // Save default config if it doesn't exist
        plugin.saveDefaultConfig();

        // Reload config
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Set defaults if they don't exist
        config.options().copyDefaults(true);
        config.addDefault("notify-players", true);
        config.addDefault("notification-message", "&cYou cannot use items with colored names in crafting recipes!");
        plugin.saveConfig();

        // Load settings
        notifyPlayers = config.getBoolean("notify-players", true);
        
        // Get notification message, use empty string if explicitly set to empty
        String configMessage = config.getString("notification-message", "&cYou cannot use items with colored names in crafting recipes!");
        if (configMessage != null && !configMessage.trim().isEmpty()) {
            notificationMessage = ChatColor.translateAlternateColorCodes('&', configMessage);
        } else {
            // If the message is empty or null, set it to empty string
            notificationMessage = "";
        }
    }

    public boolean shouldNotifyPlayers() {
        return notifyPlayers;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }
}