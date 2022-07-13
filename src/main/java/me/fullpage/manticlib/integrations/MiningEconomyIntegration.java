package me.fullpage.manticlib.integrations;

import me.fullpage.manticlib.integrations.manager.Integration;
import me.fullpage.miningeconomy.api.MiningEconomy;
import me.fullpage.miningeconomy.api.interfaces.Economy;
import me.fullpage.miningeconomy.api.interfaces.MPlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MiningEconomyIntegration extends Integration {

    private MiningEconomy miningEconomy;

    public MiningEconomyIntegration() {
        super("MiningEconomy");
        this.addRequiredClass("me.fullpage.miningeconomy.api.MiningEconomy");
    }

    @Override
    public void onEnable() {
        miningEconomy = (MiningEconomy) JavaPlugin.getProvidingPlugin(MiningEconomy.class);
    }

    public MiningEconomy getMiningEconomy() {
        return miningEconomy;
    }

    public boolean hasEnough(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;

        MPlayer mPlayer = this.getMiningEconomy().getMPlayer(uuid.toString());
        if (mPlayer == null) {
            return false;
        }

        Economy economy = mPlayer.getPlayerEconomy();
        return economy != null && economy.hasEnoughBalance(amount);
    }

    public boolean takeMoney(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;

        MPlayer mPlayer = this.getMiningEconomy().getMPlayer(uuid.toString());
        if (mPlayer == null) {
            return false;
        }

        Economy economy = mPlayer.getPlayerEconomy();
        if (economy == null) {
            return false;
        }

        economy.removeBalance(amount);
        return true;
    }
    public boolean giveMoney(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;

        MPlayer mPlayer = this.getMiningEconomy().getMPlayer(uuid.toString());
        if (mPlayer == null) {
            return false;
        }

        Economy economy = mPlayer.getPlayerEconomy();
        if (economy == null) {
            return false;
        }

        economy.addBalance(amount);
        return true;
    }

}
