package me.fullpage.manticlib.utils;

import me.fullpage.manticlib.events.armourequipevent.ArmourType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ArmourUtil {

    private static final List<Material> helmets;
    private static final List<Material> chestplates;
    private static final List<Material> leggings;
    private static final List<Material> boots;

    static {
        helmets = new ArrayList<>();
        chestplates = new ArrayList<>();
        leggings = new ArrayList<>();
        boots = new ArrayList<>();

        try {

            for (Material value : Material.values()) {
                String name = value.name();
                if (name.startsWith("LEGACY_")) {
                    continue;
                }

                if (name.endsWith("_HELMET")) {
                    helmets.add(value);
                } else if (name.endsWith("_CHESTPLATE")) {
                    chestplates.add(value);
                } else if (name.endsWith("_LEGGINGS")) {
                    leggings.add(value);
                } else if (name.endsWith("_BOOTS")) {
                    boots.add(value);
                }
            }

        } catch (Exception | Error e) {
            e.printStackTrace();
        }
    }

    public static List<Material> getBoots() {
        return Collections.unmodifiableList(boots);
    }

    public static List<Material> getLeggings() {
        return Collections.unmodifiableList(leggings);
    }

    public static List<Material> getChestplates() {
        return Collections.unmodifiableList(chestplates);
    }

    public static List<Material> getHelmets() {
        return Collections.unmodifiableList(helmets);
    }

    public static boolean isHelmet(Material material) {
        if (material == null) {
            return false;
        }
        return helmets.contains(material);
    }

    public static boolean isHelmet(ItemStack item) {
        if (item == null) {
            return false;
        }
        return isHelmet(item.getType());
    }

    public static boolean isChestplate(Material material) {
        if (material == null) {
            return false;
        }
        return chestplates.contains(material);
    }

    public static boolean isChestplate(ItemStack item) {
        if (item == null) {
            return false;
        }
        return isChestplate(item.getType());
    }

    public static boolean isLeggings(Material material) {
        if (material == null) {
            return false;
        }
        return leggings.contains(material);
    }

    public static boolean isLeggings(ItemStack item) {
        if (item == null) {
            return false;
        }
        return isLeggings(item.getType());
    }

    public static boolean isBoots(Material material) {
        if (material == null) {
            return false;
        }
        return boots.contains(material);
    }

    public static boolean isBoots(ItemStack item) {
        if (item == null) {
            return false;
        }
        return isBoots(item.getType());
    }

    public static boolean isArmour(Material material) {
        return isHelmet(material) || isChestplate(material) || isLeggings(material) || isBoots(material);
    }

    public static boolean isArmour(ItemStack item) {
        if (item == null) {
            return false;
        }
        return isArmour(item.getType());
    }

    public static boolean isArmour(Material material, ArmourType type) {
        switch (type) {
            case HELMET:
                return isHelmet(material);
            case CHESTPLATE:
                return isChestplate(material);
            case LEGGINGS:
                return isLeggings(material);
            case BOOTS:
                return isBoots(material);
            default:
                return false;
        }
    }

    public static boolean isArmour(ItemStack item, ArmourType type) {
        if (item == null) {
            return false;
        }
        return isArmour(item.getType(), type);
    }

    public static Optional<ArmourType> getArmourType(Material material) {
        if (isHelmet(material)) {
            return Optional.of(ArmourType.HELMET);
        } else if (isChestplate(material)) {
            return Optional.of(ArmourType.CHESTPLATE);
        } else if (isLeggings(material)) {
            return Optional.of(ArmourType.LEGGINGS);
        } else if (isBoots(material)) {
            return Optional.of(ArmourType.BOOTS);
        } else {
            return Optional.empty();
        }
    }

    public static Optional<ArmourType> getArmourType(ItemStack item) {
        if (item == null) {
            return Optional.empty();
        }
        return getArmourType(item.getType());
    }

    public static boolean isArmour(ItemStack item, ArmourType... types) {
        if (item == null) {
            return false;
        }
        for (ArmourType type : types) {
            if (isArmour(item, type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isArmour(Material material, ArmourType... types) {
        for (ArmourType type : types) {
            if (isArmour(material, type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isArmour(ItemStack item, List<ArmourType> types) {
        if (item == null) {
            return false;
        }
        for (ArmourType type : types) {
            if (isArmour(item, type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isArmour(Material material, List<ArmourType> types) {
        for (ArmourType type : types) {
            if (isArmour(material, type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isArmour(ItemStack itemStack, Class<? extends Material> material) {
        if (itemStack == null) {
            return false;
        }
        return material.isInstance(itemStack.getType());
    }

    public static boolean isArmour(Material material, Class<? extends Material> materialClass) {
        return materialClass.isInstance(material);
    }

    public static boolean isWearingFullSet(@NotNull Player player) {
        PlayerInventory inventory = player.getInventory();
        return isArmour(inventory.getHelmet()) && isArmour(inventory.getChestplate()) && isArmour(inventory.getLeggings()) && isArmour(inventory.getBoots());
    }

    public static boolean isWearingFullSet(@NotNull Player player, Class<? extends Material> material) {
        PlayerInventory inventory = player.getInventory();
        return isArmour(inventory.getHelmet(), material) && isArmour(inventory.getChestplate(), material) && isArmour(inventory.getLeggings(), material) && isArmour(inventory.getBoots(), material);
    }

}
