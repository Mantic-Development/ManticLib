package me.fullpage.manticlib.integrations;

import me.fullpage.infinitekoth.data.KothData;
import me.fullpage.manticlib.integrations.manager.Integration;

public class InfiniteKothIntegration extends Integration {

    public InfiniteKothIntegration() {
        super("InfiniteKoth");
        this.addRequiredClass("me.fullpage.infinitekoth.InfiniteKoth");
    }

    public String getCapper() {
        return isActive() ? KothData.getInstance().getCapper() : "";
    }

    public long getCapMillis() {
        return isActive() ? (long) KothData.getInstance().capTime * 1000 : 0L;
    }

}
