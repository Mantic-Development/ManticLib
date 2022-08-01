package me.fullpage.manticlib.data;

import de.exlll.configlib.BukkitYamlConfiguration;
import me.fullpage.manticlib.ManticLib;

public final class Config extends BukkitYamlConfiguration {
    public Config() {
        super(ManticLib.get(), "config");
    }

   public boolean autoUpdate = true;

}
