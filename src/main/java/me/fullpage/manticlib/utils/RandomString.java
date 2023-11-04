package me.fullpage.manticlib.utils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The type Random string.
 *
 * @author Fullpage12
 * apinote: Simple random string generate for letters and numbers
 */
public class RandomString {


    /**
     * Gets random int.
     *
     * @return the random int
     */
    public static int getRandomInt() {
        return ThreadLocalRandom.current().nextInt(0, 10);
    }

    /**
     * Gets random letter.
     *
     * @return the random letter
     */
    public static char getRandomLetter() {
        int rnd = (int) (ThreadLocalRandom.current().nextDouble() * 52);
        char base = (rnd < 26) ? 'A' : 'a';
        return (char) (base + rnd % 26);
    }

    /**
     * Generate string.
     *
     * @param length the length
     * @return the string
     */
    public static String generate(int length) {
        char[] array = new char[length];
        for (int i = 0; i < length; i++) {
            array[i] = ThreadLocalRandom.current().nextBoolean() ? getRandomLetter() : (char) (getRandomInt() + '0');
        }
        return new String(array);
    }

}
