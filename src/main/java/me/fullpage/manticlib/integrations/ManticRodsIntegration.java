package me.fullpage.manticlib.integrations;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.integrations.manager.Integration;
import me.fullpage.manticrods.ManticRods;
import me.fullpage.manticrods.data.MPlayers;
import me.fullpage.manticrods.wrappers.MPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ManticRodsIntegration extends Integration {

    private ManticRods manticRods;

    public ManticRodsIntegration() {
        super("ManticRods");
        this.addRequiredClass("me.fullpage.manticrods.ManticRods");
    }

    @Override
    public void onEnable() {
        manticRods = (ManticRods) ManticLib.getProvidingPlugin(ManticRods.class);
    }

    public ManticRods getManticRods() {
        return manticRods;
    }

    public long getBalance(UUID uuid) {
        if (!isActive()) return 0;
        return MPlayers.get(uuid).getShards();
    }


    public boolean hasEnough(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;

        MPlayer mPlayer = MPlayers.get(uuid);
        return mPlayer.hasEnoughShards(amount);
    }

    public boolean takeMoney(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);
        mPlayer.takeShards(amount);
        return true;
    }

    public boolean giveMoney(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);

        mPlayer.addShards(amount);
        return true;
    }

}
