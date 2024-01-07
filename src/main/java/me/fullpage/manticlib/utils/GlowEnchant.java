package me.fullpage.manticlib.utils;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.nmslib.EnchantInfo;
import me.fullpage.nmslib.plugin.NMSLib;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

public final class GlowEnchant extends EnchantInfo {
    private static GlowEnchant i;
    private static Enchantment glow;

    private GlowEnchant() {
        super("Glow", 9999);
        i = this;
    }

    public static Enchantment get() {
        if (glow != null) return glow;
        if (i == null) new GlowEnchant();

        Enchantment enchantment = NMSLib.getNmsHandler().lookupEnchantment(i.getName(), i.getInternalId());
        if (enchantment != null) return glow = enchantment;


        try {
            glow = NMSLib.getNmsHandler().registerEnchantment(i, ManticLib.get());
         //   NMSLib.getNmsHandler().registerEnchantment(glow);
            return glow;
        } catch (Throwable t) {
            glow = null;
            throw t;
        }
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getStartLevel() {
        return 1;
    }

    public EnchantmentTarget getItemTarget() {
        return EnchantmentTarget.ALL;
    }

    public boolean canEnchantItem(ItemStack item) {
        return true;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    public String getName() {
        return "Glow";
    }

    public boolean conflictsWith(Enchantment other) {
        return false;
    }

}