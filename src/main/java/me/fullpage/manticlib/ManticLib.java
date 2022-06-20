package me.fullpage.manticlib;

import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class ManticLib extends JavaPlugin {

    private static ManticLib instance;

    public static ManticLib get() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

}
