package ro.marioenache.customitemchecker.listeners;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ro.marioenache.customitemchecker.config.ConfigManager;
import ro.marioenache.customitemchecker.service.ItemValidationService;

public class CraftingListener implements Listener {

    private final JavaPlugin plugin;
    private final ItemValidationService validationService;
    private final ConfigManager configManager;

    public CraftingListener(
            JavaPlugin plugin,
            ItemValidationService validationService,
            ConfigManager configManager) {
        this.plugin = plugin;
        this.validationService = validationService;
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();

        // Check if there's a valid recipe result
        if (inventory.getResult() == null) {
            return;
        }

        // Create a snapshot of the inventory matrix to avoid concurrency issues
        ItemStack[] matrixSnapshot = inventory.getMatrix().clone();

        // Check for colored items synchronously
        boolean hasColoredItem = false;
        for (ItemStack item : matrixSnapshot) {
            if (item != null && item.hasItemMeta()) {
                // Check if this item has a colored name (using cache when available)
                if (validationService.hasColoredName(item)) {
                    hasColoredItem = true;
                    break;
                } else {
                    // Schedule async check to update/confirm cache for future crafts
                    validationService.scheduleAsyncCheck(item);
                }
            }
        }

        if (hasColoredItem) {
            // Cancel the crafting by setting the result to null
            inventory.setResult(null);
            
            // Notify player if needed
            notifyPlayerIfNeeded(event);
        }
    }

    private void notifyPlayerIfNeeded(PrepareItemCraftEvent event) {
        if (configManager.shouldNotifyPlayers()) {
            String message = configManager.getNotificationMessage();
            // Only send the message if it's not null or empty
            if (message != null && !message.trim().isEmpty()) {
                HumanEntity viewer = event.getView().getPlayer();
                if (viewer != null) {
                    // Schedule notification to run on main thread to be safe
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        viewer.sendMessage(message);
                    });
                }
            }
        }
    }
}