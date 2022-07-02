package me.fullpage.manticlib;

import lombok.Getter;
import me.fullpage.manticlib.integrations.InfiniteKothIntegration;
import me.fullpage.manticlib.integrations.VaultIntegration;

@Getter
public final class ManticLib extends ManticPlugin {

    private static ManticLib instance;

    public static ManticLib get() {
        return instance;
    }

    private InfiniteKothIntegration infiniteKoth;
    private VaultIntegration vault;

    @Override
    public void onEnable() {
        instance = this;

        infiniteKoth = new InfiniteKothIntegration();
        vault = new VaultIntegration();

    }

}
