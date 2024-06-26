package me.fullpage.manticlib.integrations;

import me.fullpage.mantichoes.ManticHoes;
import me.fullpage.mantichoes.data.MPlayers;
import me.fullpage.mantichoes.wrappers.MPlayer;
import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.integrations.manager.Integration;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ManticHoesIntegration extends Integration {

    private ManticHoes manticHoes;

    public ManticHoesIntegration() {
        super("ManticHoes");
        this.addRequiredClass("me.fullpage.mantichoes.ManticHoes");
    }

    @Override
    public void onEnable() {
        manticHoes = (ManticHoes) ManticLib.getProvidingPlugin(ManticHoes.class);
    }

    public ManticHoes getManticHoes() {
        return manticHoes;
    }

    @Deprecated
    public long getBalance(UUID uuid) {
        if (!isActive()) return 0;
        return MPlayers.get(uuid).getTokens();
    }

    public double getTokens(UUID uuid) {
        if (!isActive()) return 0;
        return MPlayers.get(uuid).getAccurateTokens();
    }

    @Deprecated
    public boolean hasEnough(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;

        MPlayer mPlayer = MPlayers.get(uuid);
        return mPlayer.hasEnoughTokens(amount);
    }

    public boolean hasEnoughTokens(@NotNull UUID uuid, double amount) {
        if (!isActive()) return false;

        MPlayer mPlayer = MPlayers.get(uuid);
        return mPlayer.hasEnoughTokens(amount);
    }

    @Deprecated
    public boolean takeMoney(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);
        mPlayer.takeTokens(amount);
        return true;
    }

    public boolean takeTokens(@NotNull UUID uuid, double amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);
        mPlayer.takeTokens(amount);
        return true;
    }

    @Deprecated
    public boolean giveMoney(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);

        mPlayer.addTokens(amount);
        return true;
    }

    public boolean giveTokens(@NotNull UUID uuid, double amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);

        mPlayer.addTokens(amount);
        return true;
    }

    @Deprecated
    public boolean setMoney(@NotNull UUID uuid, long amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);

        mPlayer.setTokens(amount);
        return true;
    }

    public boolean setTokens(@NotNull UUID uuid, double amount) {
        if (!isActive()) return false;
        MPlayer mPlayer = MPlayers.get(uuid);

        mPlayer.setTokens(amount);
        return true;
    }

}
