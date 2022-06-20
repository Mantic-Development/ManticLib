package me.fullpage.manticlib.utils;

public class MathUtils {

    /**
     * @apiNote int[0] == chunkX and int[1] == chunkZ
     */
    public static int[] chunkFromBlock(int blockX, int blockZ) {

        int chunkX = floor(blockX) >> 4;
        int chunkZ = floor(blockZ) >> 4;

        return new int[]{chunkX, chunkZ};
    }


    public static int floor(double num) {
        int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

}
