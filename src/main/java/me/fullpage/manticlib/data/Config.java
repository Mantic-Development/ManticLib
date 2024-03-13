package me.fullpage.manticlib.data;

import de.exlll.configlib.BukkitYamlConfiguration;
import de.exlll.configlib.annotation.Comment;
import lombok.Getter;
import me.fullpage.manticlib.ManticLib;

@Getter
public final class Config extends BukkitYamlConfiguration {
    public Config() {
        super(ManticLib.get(), "config");
    }

    @Comment("Settings")
    public boolean autoUpdate = true;

    @Comment({"", "Messages"})
    public String defaultNoPermissionCommand = "&cYou do not have permission to use this command.";
    public String defaultOnlyPlayersCommand = "&cOnly players can run this command.";

}
