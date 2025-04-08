package ro.marioenache.customitemchecker.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Pattern;

public class ColorChecker {

    // Pattern to match color codes (both § and & formats)
    private static final Pattern COLOR_PATTERN = Pattern.compile("[§&][0-9a-fA-Fk-oK-OrR]");

    /**
     * Checks if an item has a colored name
     *
     * @param item The item to check
     * @return true if the item has a colored name, false otherwise
     */
    public static boolean hasColoredName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        // Method 1: Check for legacy color codes
        String displayName = meta.getDisplayName();
        String strippedName = ChatColor.stripColor(displayName);
        if (!strippedName.equals(displayName)) {
            return true;
        }

        // Method 2: Check for § and & color codes directly in the name
        if (COLOR_PATTERN.matcher(displayName).find()) {
            return true;
        }

        // Method 3: Check Adventure API components for colors
        Component displayNameComponent = meta.displayName();
        if (displayNameComponent != null) {
            try {
                // Check if the component has a color
                if (displayNameComponent.color() != null) {
                    return true;
                }

                // Check children components
                if (!displayNameComponent.children().isEmpty()) {
                    return hasComponentWithColor(displayNameComponent);
                }
            } catch (Exception ignored) {
                // Safely ignore exceptions
            }
        }

        return false;
    }

    /**
     * Recursively checks if any component in the hierarchy has color
     *
     * @param component The component to check
     * @return true if the component or any of its children has color
     */
    private static boolean hasComponentWithColor(Component component) {
        if (component == null) {
            return false;
        }

        try {
            // Check this component's color
            if (component.color() != null) {
                return true;
            }

            // Check all children
            for (Component child : component.children()) {
                if (hasComponentWithColor(child)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // Safely ignore any exceptions
        }

        return false;
    }
}