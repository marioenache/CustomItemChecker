package ro.marioenache.customitemchecker.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ro.marioenache.customitemchecker.utils.ColorChecker;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ItemValidationService {

    private final JavaPlugin plugin;

    // Cache for item validation results
    private final Cache<UUID, Boolean> itemColorCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    // In-progress validation tracking to prevent redundant checks
    private final ConcurrentHashMap<UUID, Boolean> inProgressValidations = new ConcurrentHashMap<>();

    public ItemValidationService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if an item has a colored name, using cache when available
     *
     * @param item The item to check
     * @return true if the item has a colored name, false otherwise
     */
    public boolean hasColoredName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        UUID itemId = getItemUniqueId(item);

        // Check cache first
        Boolean cachedResult = itemColorCache.getIfPresent(itemId);
        if (cachedResult != null) {
            return cachedResult;
        }

        // Perform actual check (this is now synchronous for immediate results)
        boolean hasColor = ColorChecker.hasColoredName(item);

        // Cache the result
        itemColorCache.put(itemId, hasColor);

        return hasColor;
    }

    /**
     * Schedule an async check for an item to populate the cache
     *
     * @param item The item to check
     */
    public void scheduleAsyncCheck(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        UUID itemId = getItemUniqueId(item);

        // Skip if already cached or in progress
        if (itemColorCache.getIfPresent(itemId) != null || inProgressValidations.containsKey(itemId)) {
            return;
        }

        // Mark as in progress
        inProgressValidations.put(itemId, true);

        // Schedule async check
        plugin.getServer().getAsyncScheduler().runNow(plugin, task -> {
            try {
                boolean hasColor = ColorChecker.hasColoredName(item);
                itemColorCache.put(itemId, hasColor);
            } finally {
                // Remove from in-progress map
                inProgressValidations.remove(itemId);
            }
        });
    }

    /**
     * Clear the cache
     */
    public void clearCache() {
        itemColorCache.invalidateAll();
        inProgressValidations.clear();
    }

    /**
     * Generate a unique ID for an item based on its properties
     *
     * @param item The item to generate an ID for
     * @return A UUID representing the item
     */
    private UUID getItemUniqueId(ItemStack item) {
        StringBuilder sb = new StringBuilder();
        sb.append(item.getType().toString());

        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            sb.append(item.getItemMeta().getDisplayName());
        }

        return UUID.nameUUIDFromBytes(sb.toString().getBytes());
    }
}