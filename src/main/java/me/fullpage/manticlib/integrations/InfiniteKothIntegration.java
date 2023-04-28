package me.fullpage.manticlib.integrations;

import me.fullpage.infinitekoth.api.InfiniteKothAPI;
import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.integrations.manager.Integration;

public class InfiniteKothIntegration extends Integration {

    private InfiniteKothAPI infiniteKothAPI;

    public InfiniteKothIntegration() {
        super("InfiniteKoth");
        this.addRequiredClass("me.fullpage.infinitekoth.api.InfiniteKothAPI");
    }

    @Override
    public void onEnable() {
        infiniteKothAPI = (InfiniteKothAPI) ManticLib.getProvidingPlugin(InfiniteKothAPI.class);
    }

    public String getCapper() {
        return isActive() ? infiniteKothAPI.getCapper() : "";
    }

    public long getCapMillis() {
        return isActive() ? infiniteKothAPI.getCapMillis() : 0L;
    }

}
