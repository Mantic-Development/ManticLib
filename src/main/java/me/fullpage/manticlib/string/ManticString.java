package me.fullpage.manticlib.string;

import me.fullpage.manticlib.utils.ReflectionUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManticString {

    @NotNull
    private String string;

    public ManticString(@NotNull String str) {
        this.string = str;
    }

    public static ManticString of(@NotNull String str) {
        return new ManticString(str);
    }

    public ManticString replaceIgnoreCase(String value, String replacement) {
        return replacer(value, replacement, true);
    }

    public ManticString replaceIgnoreCase(String value, Number replacement) {
        return replacer(value, String.valueOf(replacement), true);
    }

    public ManticString replace(String value, String replacement) {
        return replacer(value, replacement, false);
    }


    public ManticString replace(String value, Number replacement) {
        return replacer(value, String.valueOf(replacement), false);
    }

    public boolean contains(CharSequence val) {
        return this.string.contains(val);
    }

    public boolean containsIgnoreCase(String s) {
        return this.string.toLowerCase().contains(s.toLowerCase());
    }

    public ManticString remove(String value) {
        return replacer(value, "", false);
    }

    public ManticString removeIgnoreCase(String value) {
        return replacer(value, "", true);
    }

    public String get() {
        return this.string;
    }

    public String colourise() {
        return this.colourise('&');
    }

    public String colourise(char c) {
        return ReflectionUtils.VER > 15 ? this.translateHexColorCodes(string) : ChatColor.translateAlternateColorCodes(c, string);
    }

    public String translateHexColorCodes(String message) {
        return replaceColor(message);
    }

    private static final Set<ChatColor> COLORS = Txt.set(ChatColor.values());
    private static final Pattern REPLACE_ALL_RGB_PATTERN = Pattern.compile("(&)?&#([0-9a-fA-F]{6})");
    private static final Pattern REPLACE_ALL_PATTERN = Pattern.compile("(&)?&([0-9a-fk-orA-FK-OR])");

    private static String replaceColor(String input) {
        StringBuffer legacyBuilder = new StringBuffer();
        Matcher legacyMatcher = REPLACE_ALL_PATTERN.matcher(input);
        legacyLoop:
        while (legacyMatcher.find()) {
            boolean isEscaped = legacyMatcher.group(1) != null;
            if (!isEscaped) {
                char code = legacyMatcher.group(2).toLowerCase(Locale.ROOT).charAt(0);
                for (ChatColor color : COLORS) {
                    if (color.getChar() == code) {
                        legacyMatcher.appendReplacement(legacyBuilder, ChatColor.COLOR_CHAR + "$2");
                        continue legacyLoop;
                    }
                }
            }
            // Don't change & to section sign (or replace two &'s with one)
            legacyMatcher.appendReplacement(legacyBuilder, "&$2");
        }
        legacyMatcher.appendTail(legacyBuilder);

        if (ReflectionUtils.VER > 15) {
            StringBuffer rgbBuilder = new StringBuffer();
            Matcher rgbMatcher = REPLACE_ALL_RGB_PATTERN.matcher(legacyBuilder.toString());
            while (rgbMatcher.find()) {
                boolean isEscaped = rgbMatcher.group(1) != null;
                if (!isEscaped) {
                    try {
                        final String hexCode = rgbMatcher.group(2);
                        rgbMatcher.appendReplacement(rgbBuilder, parseHexColor(hexCode));
                        continue;
                    } catch (NumberFormatException ignored) {
                    }
                }
                rgbMatcher.appendReplacement(rgbBuilder, "&#$2");
            }
            rgbMatcher.appendTail(rgbBuilder);
            return rgbBuilder.toString();
        }
        return legacyBuilder.toString();
    }

    /**
     * @throws NumberFormatException If the provided hex color code is invalid or if version is lower than 1.16.
     */
    private static String parseHexColor(String hexColor) throws NumberFormatException {
        if (ReflectionUtils.VER < 16) throw new NumberFormatException("Version is lower than 1.16");
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }
        if (hexColor.length() != 6) {
            throw new NumberFormatException("Invalid hex length");
        }

        Color.fromRGB(Integer.decode("#" + hexColor));
        final StringBuilder assembledColorCode = new StringBuilder();
        assembledColorCode.append(ChatColor.COLOR_CHAR + "x");
        for (final char curChar : hexColor.toCharArray()) {
            assembledColorCode.append(ChatColor.COLOR_CHAR).append(curChar);
        }
        return assembledColorCode.toString();
    }

    public String[] split(String regex) {
        return this.string.split(regex);
    }

    @Override
    public String toString() {
        return this.string;
    }

    private ManticString replacer(String value, String replacement, boolean ignoreCase) {

        if (string == null) {
            return null;
        }

        if (value == null || value.length() == 0) {
            return this;
        }

        final int valueLength = value.length();

        final int stringLength = string.length();
        if (valueLength > stringLength) {
            return this;
        }

        int counter = 0;
        String subString;
        while ((counter < stringLength) && (string.substring(counter).length() >= valueLength)) {
            subString = string.substring(counter, counter + valueLength);
            if (ignoreCase) {
                if (subString.equalsIgnoreCase(value)) {
                    string = string.substring(0, counter) + replacement
                            + string.substring(counter + valueLength);
                    counter += replacement.length();
                } else {
                    counter++;
                }
            } else {
                if (subString.equals(value)) {
                    string = string.substring(0, counter) + replacement
                            + string.substring(counter + valueLength);
                    counter += replacement.length();
                } else {
                    counter++;
                }
            }
        }
        return this;
    }


    /**
     * @param args are the values which will replace "{digit}"
     * @return a replaced string
     */
    public ManticString format(String... args) {
        for (int i = 0; i < args.length; i++) {
            replace("{" + i + "}", args[i]);
        }
        return this;
    }


    /**
     * @param args are the values which will replace "{digit}"
     * @return a replaced string
     */
    public ManticString format(Number... args) {
        for (int i = 0; i < args.length; i++) {
            replace("{" + i + "}", args[i]);
        }
        return this;
    }


    /**
     * @param args are the values which will replace "{digit}"
     * @return a replaced string
     */
    public ManticString format(Object... args) {
        for (int i = 0; i < args.length; i++) {
            replace("{" + i + "}", String.valueOf(args[i]));
        }
        return this;
    }


    public ManticString replaceLast(String value, String replacement) {
        int pos = string.lastIndexOf(value);
        if (pos > -1) {
            string = string.substring(0, pos)
                    + replacement
                    + string.substring(pos + value.length());
        }
        return this;
    }

    public ManticString replaceLast(char value, char replacement) {
        int pos = string.lastIndexOf(value);
        if (pos > -1) {
            string = string.substring(0, pos)
                    + replacement
                    + string.substring(pos + value);
        }
        return this;
    }

    public ManticString capitalise() {
        string = this.capitalize(string);
        return this;
    }

    public ManticString capitaliseFully() {
        string = this.capitalizeFully(string);
        return this;
    }

    public ManticString capitalise(char... delimiters) {
        string = this.capitalize(string, delimiters);
        return this;
    }

    public ManticString capitaliseFully(char... delimiters) {
        string = this.capitalizeFully(string, delimiters);
        return this;
    }

    public ManticString remove(char target) {
        char delete = 0;
        string = string.replace(Character.toString(target), "");
        return this;
    }

    public ManticString replace(char value, char replacement) {
        string = string.replace(value, replacement);
        return this;
    }


    private String capitalizeFully(String str) {
        return capitalizeFully(str, (char[]) null);
    }

    private String capitalizeFully(String str, char... delimiters) {
        if (str.isEmpty()) {
            return str;
        } else {
            str = str.toLowerCase();
            return capitalize(str, delimiters);
        }
    }

    private String capitalize(String str) {
        return capitalize(str, (char[]) null);
    }

    private String capitalize(String str, char... delimiters) {
        if (str.isEmpty()) {
            return str;
        } else {
            Set<Integer> delimiterSet = generateDelimiterSet(delimiters);
            int strLen = str.length();
            int[] newCodePoints = new int[strLen];
            int outOffset = 0;
            boolean capitalizeNext = true;
            int index = 0;

            while (index < strLen) {
                int codePoint = str.codePointAt(index);
                if (delimiterSet.contains(codePoint)) {
                    capitalizeNext = true;
                    newCodePoints[outOffset++] = codePoint;
                    index += Character.charCount(codePoint);
                } else if (capitalizeNext) {
                    int titleCaseCodePoint = Character.toTitleCase(codePoint);
                    newCodePoints[outOffset++] = titleCaseCodePoint;
                    index += Character.charCount(titleCaseCodePoint);
                    capitalizeNext = false;
                } else {
                    newCodePoints[outOffset++] = codePoint;
                    index += Character.charCount(codePoint);
                }
            }

            return new String(newCodePoints, 0, outOffset);
        }
    }

    private Set<Integer> generateDelimiterSet(char[] delimiters) {
        Set<Integer> delimiterHashSet = new HashSet<>();
        if (delimiters != null && delimiters.length != 0) {
            for (int index = 0; index < delimiters.length; ++index) {
                delimiterHashSet.add(Character.codePointAt(delimiters, index));
            }

            return delimiterHashSet;
        } else {
            if (delimiters == null) {
                delimiterHashSet.add(Character.codePointAt(new char[]{' '}, 0));
            }

            return delimiterHashSet;
        }
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNullOrEmpty(ManticString manticString) {
        return manticString == null || isNullOrEmpty(manticString.string);
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("§#[0-9A-Fa-f]+");
    public ManticString stripHexColourCodes() {
        if (!ReflectionUtils.supports(16) || string == null) {
            return this;
        }

        string =  HEX_PATTERN.matcher(string).replaceAll("");
        return this;
    }

    public ManticString stripColour() {
        string = ChatColor.stripColor(this.stripHexColourCodes().string);
        return this;
    }

    public ManticString trim() {
        string = string.trim();
        return this;
    }

    public boolean isEmpty() {
        return string.isEmpty();
    }
    
}

