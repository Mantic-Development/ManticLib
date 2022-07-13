package me.fullpage.manticlib.integrations;

import me.fullpage.mantichoes.ManticHoes;
import me.fullpage.manticlib.integrations.manager.Integration;
import org.bukkit.plugin.java.JavaPlugin;

public class ManticHoesIntegration extends Integration {

    private ManticHoes manticHoes;

    public ManticHoesIntegration() {
        super("ManticHoes");
        this.addRequiredClass("me.fullpage.mantichoes.ManticHoes");
    }

    @Override
    public void onEnable() {
        manticHoes = (ManticHoes) JavaPlugin.getProvidingPlugin(ManticHoes.class);
    }

    public ManticHoes getManticHoes() {
        return manticHoes;
    }

}
