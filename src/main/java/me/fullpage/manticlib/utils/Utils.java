package me.fullpage.manticlib.utils;

import me.fullpage.manticlib.builders.ItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Utils {


    public static boolean isNullOrEmpty(String value) {
        return (value == null) || (value.trim().length() == 0);
    }

    public static boolean isNullOrEmpty(Object value) {
        if (value instanceof Collection) {
            return isNullOrEmpty((Collection<?>) value);
        }
        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
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

    public static boolean isUuid(String input) {
        if (Utils.isNullOrEmpty(input)) {
            return false;
        }

        return input.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
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
        return removeLastZeros(DOLLAR_BALANCE_FORMAT.format(balance));
    }

    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    public static String formatNumber(Number number) {
        return removeLastZeros(NUMBER_FORMAT.format(number));
    }

    public static void main(String[] args) {
        System.out.println(removeLastZeros(formatNumber(0.0)));
    }

    private static String removeLastZeros(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        if (str.indexOf('.') != -1) {
            str = str.replaceAll("0*$", "").replaceAll("\\.$", "");
        }
        return str;
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

    public static String convertToRoman(int input) {
        if (input < 1 || input > 3999)
            return String.valueOf(input);

        StringBuilder s = new StringBuilder();

        while (input >= 1000) {
            s.append("M");
            input -= 1000;
        }
        while (input >= 900) {
            s.append("CM");
            input -= 900;
        }
        while (input >= 500) {
            s.append("D");
            input -= 500;
        }
        while (input >= 400) {
            s.append("CD");
            input -= 400;
        }
        while (input >= 100) {
            s.append("C");
            input -= 100;
        }
        while (input >= 90) {
            s.append("XC");
            input -= 90;
        }
        while (input >= 50) {
            s.append("L");
            input -= 50;
        }
        while (input >= 40) {
            s.append("XL");
            input -= 40;
        }
        while (input >= 10) {
            s.append("X");
            input -= 10;
        }
        while (input >= 9) {
            s.append("IX");
            input -= 9;
        }
        while (input >= 5) {
            s.append("V");
            input -= 5;
        }
        while (input >= 4) {
            s.append("IV");
            input -= 4;
        }
        while (input >= 1) {
            s.append("I");
            input -= 1;
        }
        return s.toString();
    }

    public static int getPing(Player p) {
        return ReflectionUtils.getPing(p);
    }

    public static void giveItems(Player player, Collection<ItemStack> items) {
        giveItems(player, items.toArray(new ItemStack[0]));
    }

    public static void giveItems(Player player, ItemStack... items) {
        giveItems(player, player.getLocation().add(0, 0.5, 0), items);
    }


    public static void giveItems(Player player, Location dropLocation, Collection<ItemStack> items) {
        giveItems(player, dropLocation, items.toArray(new ItemStack[0]));
    }

    public static void giveItems(Player player, Location dropLocation, ItemStack... items) {
        if (player == null || items == null || dropLocation == null || items.length == 0) {
            return;
        }

        for (ItemStack drop : items) {
            if (drop == null) {
                continue;
            }

            int count = drop.getAmount();
            PlayerInventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getSize(); i++) {
                if (count <= 0 || (ReflectionUtils.VER > 12 && i >= 36)) { // don't fill armor slots
                    break;
                }


                ItemStack item = inventory.getItem(i);
                int maxStackSize = item == null ? drop.getMaxStackSize() : item.getMaxStackSize();
                int toGive = Math.min(maxStackSize, count);
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
                    if (amount < maxStackSize) {
                        int toReplace = Math.min(maxStackSize - amount, toGive);
                        item.setAmount(amount + toReplace);
                        count -= toReplace;
                    }
                }
            }

            if (count > 0) {
                player.getWorld().dropItemNaturally(dropLocation, ItemBuilder.from(drop).amount(count));
            }

        }
    }

    public static void dropItemAt(Location location, ItemStack item) {
        if (location == null || item == null) {
            return;
        }

        location.getWorld().dropItemNaturally(location, item);
    }

    public static void dropItemAt(Location location, ItemStack item, int amount) {
        if (location == null || item == null) {
            return;
        }

        location.getWorld().dropItemNaturally(location, ItemBuilder.from(item).amount(amount));
    }

    public static void dropItemAt(Location location, Material material) {
        if (location == null || material == null) {
            return;
        }

        location.getWorld().dropItemNaturally(location, new ItemStack(material));
    }

    public static void dropItemAt(Location location, Material material, int amount) {
        if (location == null || material == null) {
            return;
        }

        location.getWorld().dropItemNaturally(location, ItemBuilder.from(material).amount(amount));
    }

    public static void dropItemsAt(Location location, Collection<ItemStack> items) {
        dropItemsAt(location, items.toArray(new ItemStack[0]));
    }

    public static void dropItemsAt(Location location, ItemStack... items) {
        if (location == null || items == null || items.length == 0) {
            return;
        }

        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }

            location.getWorld().dropItemNaturally(location, item);
        }
    }


    public static ItemStack glassFromNumber(int i) {
        Material glassPane = Material.matchMaterial("STAINED_GLASS_PANE");
        ItemStack glass = null;
        switch (i) {
            case 0:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("WHITE_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 0);
                }
                break;
            case 1:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("ORANGE_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 1);
                }
                break;
            case 2:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("MAGENTA_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 2);
                }
                break;
            case 3:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("LIGHT_BLUE_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 3);
                }
                break;
            case 4:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("YELLOW_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 4);
                }
                break;
            case 5:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("LIME_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 5);
                }
                break;
            case 6:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("PINK_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 6);
                }
                break;
            case 7:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("GRAY_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 7);
                }
                break;
            case 8:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("LIGHT_GRAY_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 8);
                }
                break;
            case 9:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("CYAN_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 9);
                }
                break;
            case 10:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("PURPLE_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 10);
                }
                break;
            case 11:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("BLUE_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 11);
                }
                break;
            case 12:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("BROWN_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 12);
                }
                break;
            case 13:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("GREEN_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 13);
                }
                break;
            case 14:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("RED_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 14);
                }
                break;
            case 15:
                if (glassPane == null) {
                    Material material = Material.matchMaterial("BLACK_STAINED_GLASS_PANE");
                    if (material != null) {
                        glass = new ItemStack(material);
                    }
                } else {
                    glass = new ItemStack(glassPane, 1, (short) 15);
                }
                break;
            default:
                return glassFromNumber(7);
        }
        return glass;
    }


}
