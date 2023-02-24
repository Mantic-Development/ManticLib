package me.fullpage.manticlib.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RandomMaterials {

    private static final List<Material> materials;

    static {
        materials = new ArrayList<>();
        Optional<Method> isSolid = ReflectionUtils.findMethod(Material.class, "isSolid");
        Optional<Method> isItem = ReflectionUtils.findMethod(Material.class, "isItem");
        Optional<Method> isTransparent = ReflectionUtils.findMethod(Material.class, "isTransparent");
        Optional<Method> isInteractable = ReflectionUtils.findMethod(Material.class, "isInteractable");
        boolean stoneBricks = false;
        for (Material material : Material.values()) {
            if (material == null || material.name().contains("AIR") || material.name().startsWith("LEGACY_")) {
                continue;
            }
            if (material.name().endsWith("STONE_BRICKS")) {
                if (stoneBricks) {
                    continue;
                }
                stoneBricks = true;
            }

            try {

                if (isTransparent.isPresent() && isTransparent.get().invoke(material) == Boolean.TRUE) {
                    continue;
                }

                if (isInteractable.isPresent() && isInteractable.get().invoke(material) == Boolean.FALSE) {
                    continue;
                }

                if (isSolid.isPresent() && isSolid.get().invoke(material) == Boolean.FALSE) {
                    continue;
                }

                if (isItem.isPresent() && isItem.get().invoke(material) == Boolean.FALSE) {
                    continue;
                }

                if (!canSpawn(material)) {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            materials.add(material);


        }
    }

    public static List<Material> getAll() {
        return materials;
    }

    public static Material getRandom() {
        return materials.get((int) (Math.random() * materials.size()));
    }

    private static boolean canSpawn(Material material) {
        try {
            Bukkit.getServer().getUnsafe().modifyItemStack(new ItemStack(material), "{}");
            return true;
        } catch (final NoSuchMethodError nsme) {
            return true;
        } catch (final Throwable npe) {
            return false;
        }
    }

}
