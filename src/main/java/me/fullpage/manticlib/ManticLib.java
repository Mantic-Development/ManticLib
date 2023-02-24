package me.fullpage.manticlib;

import lombok.Getter;
import me.fullpage.manticlib.data.Config;
import me.fullpage.manticlib.integrations.*;
import me.fullpage.manticlib.integrations.manager.Integration;
import me.fullpage.manticlib.listeners.ArmourListener;
import me.fullpage.manticlib.listeners.DispenserArmorListener;
import me.fullpage.manticlib.listeners.PlayerMoveListener;
import me.fullpage.manticlib.utils.RandomMaterials;
import me.fullpage.nmslib.NMSHandler;
import me.fullpage.nmslib.plugin.NMSLib;
import org.bukkit.Material;

import java.util.List;

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
    private ManticRodsIntegration manticRods;

    private Config configuration;

    // TODO: 01/08/2022 implement update checker task

    @Override
    public void onEnable() {
        instance = this;

        try {

            ArmourListener armourListener = new ArmourListener();
            try {
                Class.forName("org.bukkit.event.block.BlockDispenseArmorEvent");
                DispenserArmorListener dispenserArmorListener = new DispenserArmorListener();
            } catch (Exception e) {
                // ignore
            }

            PlayerMoveListener playerMoveListener = new PlayerMoveListener();
        } catch (Exception ignored) {
        }

        try {
            configuration = new Config();
            configuration.loadAndSave();
            if (configuration.autoUpdate) {
                Versionator.updateToLatest(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TODO: 22/07/2022 fix block setting
        nmsHandler = NMSLib.init(this);
        infiniteKoth = new InfiniteKothIntegration();
        vault = new VaultIntegration();
        miningEconomy = new MiningEconomyIntegration();
        manticHoes = new ManticHoesIntegration();
        manticSwords = new ManticSwordsIntegration();
        manticRods = new ManticRodsIntegration();

        List<Material> all = RandomMaterials.getAll();// init

    }

    @Override
    public void onInnerDisable() {
        for (Integration integration : Integration.INTEGRATIONS) {
            if (integration != null) {
                try {
                    integration.forceDisable();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
