package me.fullpage.manticlib;

import lombok.Getter;
import me.fullpage.manticlib.bstats.Metrics;
import me.fullpage.manticlib.command.ManticCommand;
import me.fullpage.manticlib.command.impl.ManticLibCmd;
import me.fullpage.manticlib.data.Config;
import me.fullpage.manticlib.integrations.*;
import me.fullpage.manticlib.integrations.manager.Integration;
import me.fullpage.manticlib.listeners.PlayerMoveListener;
import me.fullpage.manticlib.listeners.armour.ArmourListener;
import me.fullpage.manticlib.listeners.armour.DispenserArmorListener;
import me.fullpage.manticlib.utils.GlowEnchant;
import me.fullpage.manticlib.utils.RandomMaterials;
import me.fullpage.nmslib.NMSHandler;
import me.fullpage.nmslib.plugin.NMSLib;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.logging.Level;

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
    @Override
    public void onEnable() {
        instance = this;

        try {
            new Metrics(this, 20018);
        } catch (Throwable ignored) {
        }

        nmsHandler = NMSLib.init(this);
        try {
            Enchantment enchantment = GlowEnchant.get();
        } catch (Throwable e) {
            getLogger().log(Level.SEVERE, "Failed to register glow enchantment", e);
        }

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

        infiniteKoth = new InfiniteKothIntegration();
        vault = new VaultIntegration();
        miningEconomy = new MiningEconomyIntegration();
        manticHoes = new ManticHoesIntegration();
        manticSwords = new ManticSwordsIntegration();
        manticRods = new ManticRodsIntegration();

        List<Material> all = RandomMaterials.getAll();// init

        ManticCommand.register(new ManticLibCmd());

    }

    @Override
    public void onInnerDisable() {
        for (Integration integration : Integration.INTEGRATIONS) {
            if (integration != null) {
                try {
                    integration.forceDisable();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
