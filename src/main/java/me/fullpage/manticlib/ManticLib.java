package me.fullpage.manticlib;

import lombok.Getter;
import me.fullpage.manticlib.integrations.*;
import me.fullpage.manticlib.integrations.manager.Integration;
import me.fullpage.nmslib.NMSHandler;
import me.fullpage.nmslib_plugin.NMSLib;

@Getter
public final class ManticLib extends ManticPlugin {

    private static ManticLib instance;

    public static ManticLib get() {
        return instance;
    }
    private NMSHandler nmsHandler;
    private InfiniteKothIntegration infiniteKoth;
    private VaultIntegration vault;
    private MiningEconomyIntegration miningEconomy;
    private ManticHoesIntegration manticHoes;
    private ManticSwordsIntegration manticSwords;

    @Override
    public void onEnable() {
        instance = this;

        nmsHandler = NMSLib.init(this);
        infiniteKoth = new InfiniteKothIntegration();
        vault = new VaultIntegration();
        miningEconomy = new MiningEconomyIntegration();
        manticHoes = new ManticHoesIntegration();
        manticSwords = new ManticSwordsIntegration();

    }

    @Override
    public void onInnerDisable() {
        for (Integration integration : Integration.INTEGRATIONS) {
            if (integration != null) {
                try{
                    integration.forceDisable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
