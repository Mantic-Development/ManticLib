package me.fullpage.manticlib.events.armourequipevent;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Map;

public enum ArmourType {
    HELMET(5), CHESTPLATE(6), LEGGINGS(7), BOOTS(8);

    private final int slot;
    private static final Map<Material, ArmourType> BY_MATERIAL = new EnumMap<>(Material.class);

    static {
        // getEnumConstants()/EnumMap, not values()/ordinal(): Commodore rewrites those for api-version plugins and drops/throws on LEGACY_*
        for (Material material : Material.class.getEnumConstants()) {
            ArmourType type = matchName(material.name());
            if (type != null) BY_MATERIAL.put(material, type);
        }
    }

    ArmourType(int slot){
        this.slot = slot;
    }

    /**
     * Attempts to match the ArmourType for the specified ItemStack.
     *
     * @param itemStack The ItemStack to parse the type of.
     * @return The parsed ArmourType, or null if not found.
     */
    public static ArmourType matchType(final ItemStack itemStack){
        if(isAirOrNull(itemStack)) return null;
        return BY_MATERIAL.get(itemStack.getType());
    }

    private static ArmourType matchName(String type){
        if(type.endsWith("_HELMET") || type.endsWith("_SKULL") || type.endsWith("_HEAD")) return HELMET;
        else if(type.endsWith("_CHESTPLATE") || type.equals("ELYTRA")) return CHESTPLATE;
        else if(type.endsWith("_LEGGINGS")) return LEGGINGS;
        else if(type.endsWith("_BOOTS")) return BOOTS;
        else return null;
    }

    public int getSlot(){
        return slot;
    }
    /**
     * A utility method to support versions that use null or air ItemStacks.
     */
    public static boolean isAirOrNull(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }
}