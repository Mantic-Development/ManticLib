package me.fullpage.manticlib.test;

import de.exlll.configlib.BukkitYamlConfiguration;

import java.util.List;

public class BasicConfig extends BukkitYamlConfiguration {
    public BasicConfig() {
        super(null, "test.yml");
    }

    public void nothing() {
        List<Object> objects = fixList(null, null);
    }
}
