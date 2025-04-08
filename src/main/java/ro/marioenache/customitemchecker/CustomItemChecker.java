package ro.marioenache.customitemchecker;

import org.bukkit.plugin.java.JavaPlugin;
import ro.marioenache.customitemchecker.config.ConfigManager;
import ro.marioenache.customitemchecker.listeners.CraftingListener;
import ro.marioenache.customitemchecker.service.ItemValidationService;

public class CustomItemChecker extends JavaPlugin {

    private ConfigManager configManager;
    private ItemValidationService validationService;

    @Override
    public void onEnable() {
        // Initialize config
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize services
        validationService = new ItemValidationService(this);

        // Create and register listeners
        CraftingListener craftingListener = new CraftingListener(
                this, validationService, configManager);

        getServer().getPluginManager().registerEvents(craftingListener, this);

        getLogger().info("CustomItemChecker plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Clean up resources
        if (validationService != null) {
            validationService.clearCache();
        }

        getLogger().info("CustomItemChecker plugin has been disabled!");
    }
}