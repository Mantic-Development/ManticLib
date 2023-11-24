package me.fullpage.manticlib.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {

    /**
     * @apiNote int[0] == chunkX and int[1] == chunkZ
     */
    public static int[] chunkFromBlock(int blockX, int blockZ) {

        int chunkX = floor(blockX) >> 4;
        int chunkZ = floor(blockZ) >> 4;

        return new int[]{chunkX, chunkZ};
    }


    /**
     * @param num The number to floor.
     * @return The floor of the number.
     */
    public static int floor(double num) {
        int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    public static boolean isInRange(double num, double min, double max) {
        return num >= min && num <= max;
    }

    /**
     * @param number the number you wish to input
     * @return will return an int that is rounded up for down randomly
     * with the decimal place deciding the change of moving up / down
     */
    public static int fairIntFromDouble(double number) {
        double decimal = number - Math.floor(number);
        if (ThreadLocalRandom.current().nextDouble() < decimal) {
            return (int) Math.ceil(number);
        }
        return (int) Math.floor(number);
    }

    /**
     * @param number the number you wish to input
     * @return will return an int that is rounded up for down randomly
     * with the decimal place deciding the change of moving up / down
     */
    public static long fairLongFromDouble(double number) {
        double decimal = number - Math.floor(number);
        if (ThreadLocalRandom.current().nextDouble() < decimal) {
            return (long) Math.ceil(number);
        }
        return (long) Math.floor(number);
    }

    /**
     * @param input  is the double you wish to check
     * @param first  is the first double that the input must be between
     * @param second is the second double the input needs to be between
     * @return will return true if input is between first and second
     */
    public static boolean isBetween(double input, double first, double second) {
        return input <= Math.max(first, second) && input >= Math.min(first, second);
    }

    /**
     * @param input  is the float you wish to check
     * @param first  is the first float that the input must be between
     * @param second is the second float the input needs to be between
     * @return will return true if input is between first and second
     */
    private static boolean isBetween(float input, float first, float second) {
        return input <= Math.max(first, second) && input >= Math.min(first, second);
    }

    /**
     * @param input  is the int you wish to check
     * @param first  is the first int that the input must be between
     * @param second is the second int the input needs to be between
     * @return will return true if input is between first and second
     */
    public static boolean isBetween(int input, int first, int second) {
        return input <= Math.max(first, second) && input >= Math.min(first, second);
    }

    /**
     * @param input  is the long you wish to check
     * @param first  is the first long that the input must be between
     * @param second is the second long the input needs to be between
     * @return will return true if input is between first and second
     */
    public static boolean isBetween(long input, long first, long second) {
        return input <= Math.max(first, second) && input >= Math.min(first, second);
    }

    /**
     *
     * @param x1 is the first x location
     * @param x2 is the second x location
     * @param z1 is the first z location
     * @param z2 is the second z location
     * @return will return the 2d distance between the two locations (Will not take Y into account)
     */
    public static double get2dDistance(double x1, double x2, double z1, double z2) {
        return Math.sqrt(Math.pow(x1 - x2, 2.0) + Math.pow(z1 - z2, 2.0));
    }

    /**
     *
     * @param loc1 is the first location
     * @param loc2 is the second location
     * @return will return the 2d distance between the two locations (Will not take Y into account)
     */
    public static double get2dDistance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2.0) + Math.pow(loc1.getX() - loc2.getX(), 2.0));
    }

    /**
     *
     * @param x1 is the first x location
     * @param x2 is the second x location
     * @param y1 is the first y location
     * @param y2 is the second y location
     * @param z1 is the first z location
     * @param z2 is the second z location
     * @return will return the 3d distance between the two locations (Will take Y into account)
     */
    public static double get3dDistance(double x1, double x2, double y1, double y2, double z1, double z2) {
        return Math.sqrt(Math.pow(x1 - x2, 2.0) + Math.pow(y1 - y2, 2.0) + Math.pow(z1 - z2, 2.0));
    }

    /**
     *
     * @param loc1 is the first location
     * @param loc2 is the second location
     * @return will return the 3d distance between the two locations (Will take Y into account)
     */
    public static double get3dDistance(Location loc1, Location loc2) {
        return Math.sqrt(Math.pow(loc1.getX() - loc2.getX(), 2.0) + Math.pow(loc1.getY() - loc2.getY(), 2.0) + Math.pow(loc1.getZ() - loc2.getZ(), 2.0));
    }

    /**
     *
     * @param point1 is the first point
     * @param point2    is the second point
     * @return
     */
    public static float getAngle(Vector point1, Vector point2) {
        double dx = point2.getX() - point1.getX();
        double dz = point2.getZ() - point1.getZ();
        float angle = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        if (angle < 0.0F) {
            angle += 360.0F;
        }
        return angle;
    }

    public Optional<Integer> getInteger(String input) {
        try {
            return Optional.of(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Double> getDouble(String input) {
        try {
            return Optional.of(Double.parseDouble(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Float> getFloat(String input) {
        try {
            return Optional.of(Float.parseFloat(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Long> getLong(String input) {
        try {
            return Optional.of(Long.parseLong(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Short> getShort(String input) {
        try {
            return Optional.of(Short.parseShort(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<Byte> getByte(String input) {
        try {
            return Optional.of(Byte.parseByte(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }



}
