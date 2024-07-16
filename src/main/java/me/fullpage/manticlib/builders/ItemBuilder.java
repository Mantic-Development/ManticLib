package me.fullpage.manticlib.builders;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.gui.GuiItem;
import me.fullpage.manticlib.string.ManticString;
import me.fullpage.manticlib.string.Txt;
import me.fullpage.manticlib.utils.GlowEnchant;
import me.fullpage.manticlib.utils.ReflectionUtils;
import me.fullpage.manticlib.utils.SkullUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This is a chainable builder for {@link ItemStack}s in {@link Bukkit}
 * <br>
 * Example Usage:<br>
 * {@code ItemStack is = new ItemBuilder(Material.LEATHER_HELMET).amount(2).data(4).durability(4).enchantment(Enchantment.ARROW_INFINITE).enchantment(Enchantment.LUCK, 2).name(ChatColor.RED + "the name").lore(ChatColor.GREEN + "line 1").lore(ChatColor.BLUE + "line 2").color(Color.MAROON);
 *
 * @author MiniDigger, computerwizjared
 * @version 1.2
 */
public class ItemBuilder extends ItemStack {

    public static ItemBuilder from(@NotNull Material material) {
        return new ItemBuilder(material);
    }

    public static ItemBuilder from(@NotNull ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public static ItemBuilder fromBase64(@NotNull String base64) {
        return new ItemBuilder(SkullUtils.getSkull(base64));
    }

    private static Method setEnchantmentGlintOverride = null;

    public static boolean hasGlintOverride() {
        return setEnchantmentGlintOverride != null;
    }

    static {
        try {
            setEnchantmentGlintOverride = ItemMeta.class.getDeclaredMethod("setEnchantmentGlintOverride", Boolean.class);
        } catch (Throwable e) {
            setEnchantmentGlintOverride = null;
        }
    }

    /**
     * Initializes the builder with the given {@link Material}
     *
     * @param mat the {@link Material} to start the builder from
     * @since 1.0
     */
    public ItemBuilder(final Material mat) {
        super(mat);
    }

    /**
     * Inits the builder with the given {@link ItemStack}
     *
     * @param is the {@link ItemStack} to start the builder from
     * @since 1.0
     */
    public ItemBuilder(final ItemStack is) {
        super(is);
    }

    /**
     * Changes the amount of the {@link ItemStack}
     *
     * @param amount the new amount to set
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder amount(final int amount) {
        setAmount(amount);
        return this;
    }

    /**
     * Changes the display name of the {@link ItemStack}
     *
     * @param name the new display name to set
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder name(final String name) {
        final ItemMeta meta = getItemMeta();
        meta.setDisplayName(new ManticString(name).colourise());
        setItemMeta(meta);
        return this;
    }

    /**
     * Adds a new list to the lore of the {@link ItemStack}
     *
     * @param text the new line to add
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder setLore(final List<String> text) {
        final ItemMeta meta = getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        for (String s : text) {
            lore.add(new ManticString(s).colourise());
        }
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    /**
     * Adds a new list to the lore of the {@link ItemStack}
     *
     * @param text the new line to add
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder lore(final List<String> text) {
        final ItemMeta meta = getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        for (String s : text) {
            lore.add(new ManticString(s).colourise());
        }
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    /**
     * Adds a new line to the lore of the {@link ItemStack}
     *
     * @param text the new line to add
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder lore(final String text) {
        final ItemMeta meta = getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(new ManticString(text).colourise());
        meta.setLore(lore);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String... text) {
        this.lore(Txt.list(text));
        return this;
    }

    /**
     * Changes the durability of the {@link ItemStack}
     *
     * @param durability the new durability to set
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder durability(final int durability) {
        setDurability((short) durability);
        return this;
    }

    /**
     * Changes the data of the {@link ItemStack}
     *
     * @param data the new data to set
     * @return this builder for chaining
     * @since 1.0
     */
    @SuppressWarnings("deprecation")
    public ItemBuilder data(final int data) {
        setData(new MaterialData(getType(), (byte) data));
        return this;
    }

    /**
     * Adds an {@link Enchantment} with the given level to the {@link ItemStack}
     *
     * @param enchantment the enchantment to add
     * @param level       the level of the enchantment
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder enchantment(final Enchantment enchantment, final int level) {
        addUnsafeEnchantment(enchantment, level);
        return this;
    }

    /**
     * Adds an {@link Enchantment} with the level 1 to the {@link ItemStack}
     *
     * @param enchantment the enchantment to add
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder enchantment(final Enchantment enchantment) {
        addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    /**
     * Changes the {@link Material} of the {@link ItemStack}
     *
     * @param material the new material to set
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder type(final Material material) {
        setType(material);
        return this;
    }

    /**
     * Clears the lore of the {@link ItemStack}
     *
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder clearLore() {
        final ItemMeta meta = getItemMeta();
        meta.setLore(new ArrayList<>());
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder glow(boolean hide) {
        /*final ItemMeta itemMeta = getItemMeta();
        itemMeta.addEnchant(Enchantment.OXYGEN, 1, true);
        setItemMeta(itemMeta);
        if (hide) {
            addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }*/
        ;
        return this.glow();
    }

    public ItemBuilder glow() {
        if (setEnchantmentGlintOverride == null) {
            enchantment(GlowEnchant.get());
        } else {
            final ItemMeta itemMeta = getItemMeta();
            try {
                setEnchantmentGlintOverride.invoke(itemMeta, true);
            } catch (Throwable e) {
                ManticLib.get().getLogger().log(Level.WARNING, "Failed to set enchantment glint override", e);
                enchantment(GlowEnchant.get());
            }
            setItemMeta(itemMeta);
        }
        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... itemFlags) {
        final ItemMeta meta = getItemMeta();
        meta.addItemFlags(itemFlags);
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder removeItemFlags(ItemFlag... itemFlags) {
        final ItemMeta meta = getItemMeta();
        meta.removeItemFlags(itemFlags);
        setItemMeta(meta);
        return this;
    }

    /**
     * Clears the list of {@link Enchantment}s of the {@link ItemStack}
     *
     * @return this builder for chaining
     * @since 1.0
     */
    public ItemBuilder clearEnchantments() {
        getEnchantments().keySet().forEach(this::removeEnchantment);
        return this;
    }

    /**
     * Sets the {@link Color} of a part of leather armor
     *
     * @param color the {@link Color} to use
     * @return this builder for chaining
     * @since 1.1
     */
    public ItemBuilder color(Color color) {
        if (getType() == Material.LEATHER_BOOTS || getType() == Material.LEATHER_CHESTPLATE || getType() == Material.LEATHER_HELMET
                || getType() == Material.LEATHER_LEGGINGS) {
            LeatherArmorMeta meta = (LeatherArmorMeta) getItemMeta();
            meta.setColor(color);
            setItemMeta(meta);
            return this;
        } else {
            throw new IllegalArgumentException("color() only applicable for leather armor!");
        }
    }

    public GuiItem asGuiItem() {
        return new GuiItem(this, null);
    }

    public String serialise() {
        return this.toBase64();
    }

    private String toBase64() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeInt(1);
            dataOutput.writeObject(new ItemStack(this));
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stack.", e);
        }
    }

    @Nullable
    public static ItemBuilder deserialise(String data) {
        ItemStack[] itemStacks = stacksFromBase64(data);
        if (itemStacks == null || itemStacks.length == 0) {
            return null;
        }
        return from(itemStacks[0]);
    }

    private static ItemStack[] stacksFromBase64(String data) {
        if (data == null || Base64Coder.decodeLines(data) == null)
            return new ItemStack[]{};

        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream dataInput = null;
        ItemStack[] stacks = null;

        try {
            dataInput = new BukkitObjectInputStream(inputStream);
            stacks = new ItemStack[dataInput.readInt()];
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        for (int i = 0; i < stacks.length; i++) {
            try {
                stacks[i] = (ItemStack) dataInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                try {
                    dataInput.close();
                } catch (IOException ignored) {
                }
                return null;
            }
        }

        try {
            dataInput.close();
        } catch (IOException ignored) {
        }

        return stacks;
    }

    private static Method SET_CUSTOM_MODEL_DATA = null;
    private static boolean CUSTOM_MODEL_DATA_CHECKED = false;

    /**
     * Needs 1.14+ to work.
     * Sets the custom model data.
     * CustomModelData is an integer that may be associated client side with a custom item model.
     * Params:
     * data – the data to set, or null to clear
     */
    public ItemBuilder customModelData(Integer data) { // 1.14+
        if (!ReflectionUtils.supports(14) || (SET_CUSTOM_MODEL_DATA == null && CUSTOM_MODEL_DATA_CHECKED)) {
            return this;
        }

        if (SET_CUSTOM_MODEL_DATA == null) {
            try {
                SET_CUSTOM_MODEL_DATA = ItemMeta.class.getMethod("setCustomModelData", Integer.class);
                CUSTOM_MODEL_DATA_CHECKED = true;
            } catch (Exception e) {
                CUSTOM_MODEL_DATA_CHECKED = true;
                return this;
            }
        }

        ItemMeta meta = getItemMeta();
        try {
            SET_CUSTOM_MODEL_DATA.invoke(meta, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setItemMeta(meta);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        ItemMeta meta = getItemMeta();
        meta.setUnbreakable(unbreakable);
        ItemMeta itemMeta = getItemMeta();

        if (ReflectionUtils.supports(9)) {
            itemMeta.setUnbreakable(unbreakable);
        } else {
            try {
                Method instanceMethod = itemMeta.getClass().getMethod("spigot");
                instanceMethod.setAccessible(true);

                Object instance = instanceMethod.invoke(itemMeta);
                Method unbreakableMethod = instance.getClass().getMethod("setUnbreakable", boolean.class);
                unbreakableMethod.setAccessible(true);
                unbreakableMethod.invoke(instance, unbreakable);
            } catch (Throwable exception) {
                exception.printStackTrace();
            }
        }

        setItemMeta(meta);
        setItemMeta(itemMeta);
        return this;
    }


}