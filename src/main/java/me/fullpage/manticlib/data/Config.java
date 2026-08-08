package me.fullpage.manticlib.data;

import de.exlll.configlib.BukkitYamlConfiguration;
import de.exlll.configlib.annotation.Comment;
import de.exlll.configlib.annotation.ElementType;
import lombok.Getter;
import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.string.Txt;
import me.fullpage.manticlib.wrappers.ForwardingData;

import java.util.List;

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


    @Comment({"",
            "Command Forwarding",
            "",
            "target = the command you wish to execute",
            "aliases = alternative commands that you'd like to execute the target command"
    })
    @ElementType(ForwardingData.class)
    public List<ForwardingData> forwardingData = Txt.list(new ForwardingData("manticdarkzone top", Txt.list("dztop", "mdztop")));

}
