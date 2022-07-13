package me.fullpage.manticlib.integrations;

import me.fullpage.manticlib.integrations.manager.Integration;
import me.fullpage.miningeconomy.api.MiningEconomy;
import org.bukkit.plugin.java.JavaPlugin;

public class MiningEconomyIntegration extends Integration {

    private MiningEconomy miningEconomy;

    public MiningEconomyIntegration() {
        super("InfiniteKoth");
        this.addRequiredClass("me.fullpage.miningeconomy.api.MiningEconomy");
    }

    @Override
    public void onEnable() {
        miningEconomy = (MiningEconomy) JavaPlugin.getProvidingPlugin(MiningEconomy.class);
    }

    public MiningEconomy getMiningEconomy() {
        return miningEconomy;
    }

}
