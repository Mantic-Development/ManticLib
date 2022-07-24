package me.fullpage.manticlib.utils;

import lombok.SneakyThrows;
import me.fullpage.manticlib.builders.ItemBuilder;
import me.fullpage.manticlib.string.ManticString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Utils {


    public static boolean isNullOrEmpty(String value) {
        return (value == null) || (value.trim().length() == 0);
    }

    public static boolean isNullOrEmpty(Object value) {
        if (value instanceof Collection) {
            return isNullOrEmpty((Collection<?>) value);
        }
        return value == null;
    }

    public static <T> boolean isNullOrEmpty(Collection<T> collection) {
        return (collection == null) || (collection.isEmpty());
    }

    public static boolean isNullOrEmpty(Number number) {
        return (number == null) || (!(number.doubleValue() > 0));
    }

    public static boolean isNullOrEmpty(Date data) {
        return data == null;
    }

    public static <T> boolean isNullOrEmpty(Map<T, T> map) {
        return (map == null) || (map.isEmpty());
    }

    public static boolean isNullOrEmpty(File file) {
        return isNull(file) || file.length() == 0;
    }

    public static boolean isNullOrEmpty(Object[] array) {
        return (array == null) || (array.length == 0);
    }

    public static boolean isNull(Object value) {

        return value == null;
    }


    private boolean isNear(int chunkX, int chunkZ, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                final int newX = chunkX + x;
                final int newZ = chunkZ + z;
                if (newX == chunkX && newZ == chunkZ) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isInt(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String input) {
        try {
            Double.parseDouble(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isLong(String input) {
        try {
            Long.parseLong(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isFloat(String input) {
        try {
            Float.parseFloat(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isBoolean(String input) {
        return Boolean.parseBoolean(input);
    }

    public static final DecimalFormat DOLLAR_BALANCE_FORMAT = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.US);

    public static String formatBalance(double balance) {
        DOLLAR_BALANCE_FORMAT.setNegativePrefix("-$");
        DOLLAR_BALANCE_FORMAT.setNegativeSuffix("");
        final String format = DOLLAR_BALANCE_FORMAT.format(balance);
        return format == null ? format : new ManticString(format).replaceLast(".00", "").toString();
    }

    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public static String formatNumber(Number number) {
        final String format = NUMBER_FORMAT.format(number);
        if (format.endsWith(".00")) {
            return new ManticString(format).replaceLast(".00", "").get();
        }
        return format;
    }

    public static final DecimalFormat VALUE_FORMAT = new DecimalFormat("#.##");

    public static String formatValue(double value) {
        String[] arr = {"", "k", "m", "b", "t", "aa", "ab", "ac", "ad", "ae", "af", "ag", "ah", "ai", "aj", "ak", "al", "am", "an", "ao", "ap", "aq", "ar", "as", "at", "au", "av", "aw", "ax", "ay", "az"};
        int index = 0;
        while ((value / 1000) >= 1) {
            value = value / 1000;
            index++;
        }
        final boolean cannotFormat = index + 1 > arr.length;
        return String.format("%s%s", cannotFormat ? "" : VALUE_FORMAT.format(value), cannotFormat ? NUMBER_FORMAT.format(value) : arr[index]);
    }

    @SneakyThrows
    public static int getPing(Player p) {
        final String version = ReflectionUtils.VERSION;
        final Class<? extends Player> playerClass = p.getClass();
        if (!playerClass.getName().equals("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer")) { //compatibility with some plugins
            p = Bukkit.getPlayer(p.getUniqueId()); //cast to org.bukkit.entity.Player
        }

        if (p == null) {
            return -1;
        }

        final Object handle = ReflectionUtils.getHandle(p);
        if (handle != null) {
            final Optional<Field> ping = ReflectionUtils.findField(handle.getClass(), "ping");
            if (ping.isPresent()) {
                return ping.get().getInt(handle);
            }
        }

        final Optional<Method> getPing = ReflectionUtils.findMethod(ReflectionUtils.CRAFT_PLAYER, "getPing");
        if (getPing.isPresent()) {
            return (int) getPing.get().invoke(p);
        }

        return -1;
    }

    public static void giveItems(Player player, ItemStack... items) {
        if (player == null || items == null || items.length == 0) {
            return;
        }

        for (ItemStack drop : items) {
            if (drop == null) {
                continue;
            }

            int count = drop.getAmount();
            PlayerInventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getSize(); i++) {
                if (count <= 0) {
                    break;
                }
                int toGive = Math.min(64, count);
                ItemStack item = inventory.getItem(i);
                if (item == null) {
                    inventory.setItem(i, ItemBuilder.from(drop).amount(toGive));
                    count -= toGive;
                    continue;
                }

                Material material = item.getType();
                if (material == Material.AIR) {
                    inventory.setItem(i, ItemBuilder.from(drop).amount(toGive));
                    count -= toGive;
                    continue;
                }

                if (drop.isSimilar(item)) {
                    int amount = item.getAmount();
                    if (amount < 64) {
                        int toReplace = Math.min(64 - amount, toGive);
                        item.setAmount(amount + toReplace);
                        count -= toReplace;
                    }
                }
            }
            if (count > 0) {
                player.getWorld().dropItemNaturally(player.getLocation().add(0, 0.5,0), ItemBuilder.from(drop).amount(count));
            }
        }
    }


}
