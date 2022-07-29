package me.fullpage.manticlib.utils;

import org.bukkit.Material;

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
        for (Material material : Material.values()) {
            if (material == null) {
                continue;
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

}
