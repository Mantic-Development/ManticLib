package me.fullpage.manticlib.string;

import org.bukkit.ChatColor;

import java.util.HashSet;
import java.util.Set;

public class ManticString {

    private String string;

    public ManticString(String str) {
        this.string = str;
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
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public String colourise(char c) {
        return ChatColor.translateAlternateColorCodes(c, string);
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
}

