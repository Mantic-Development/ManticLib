package me.fullpage.manticlib.utils.despical.minecraft;

import me.fullpage.manticlib.utils.ReflectionUtils;

/**
 * <a href="https://github.com/CryptoMorin/XSeries/tree/master/src/main/java/com/cryptomorin/xseries/reflection">Original Source Code</a>
 *
 * @author CryptoMorin
 * @author Despical
 * <p>
 * Created at 23.05.2024
 */
public enum MinecraftPackage {

    NMS(ReflectionUtils.NMS_PACKAGE),
    CB(ReflectionUtils.CRAFTBUKKIT_PACKAGE),
    SPIGOT("org.spigotmc");

    private final String packageId;

    MinecraftPackage(String packageName) {
        this.packageId = packageName;
    }

    public String getPackageId() {
        return packageId;
    }
}