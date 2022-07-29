package me.fullpage.manticlib.integrations;

import me.fullpage.manticsword.data.MPlayers;
import me.fullpage.manticsword.wrappers.MPlayer;
import me.fullpage.manticlib.integrations.manager.Integration;
import me.fullpage.manticsword.ManticSwords;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ManticSwordsIntegration extends Integration {

    private ManticSwords manticSwords;

    public ManticSwordsIntegration() {
        super("ManticSwords");
        this.addRequiredClass("me.fullpage.manticsword.ManticSwords");
    }

    @Override
    public void onEnable() {
        manticSwords = (ManticSwords) JavaPlugin.getProvidingPlugin(ManticSwords.class);
    }

    public ManticSwords getManticSwords() {
        return manticSwords;
    }

    public boolean hasEnough(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;

        MPlayer mPlayer = MPlayers.get(uuid);
        return mPlayer.hasEnoughCrystals(amount);
    }

    public boolean takeMoney(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);
        mPlayer.takeCrystals(amount);
        return true;
    }

    public boolean giveMoney(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);

        mPlayer.addCrystals(amount);
        return true;
    }

}
