package me.fullpage.manticlib.integrations;

import me.fullpage.manticlib.integrations.manager.Integration;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

public class VaultIntegration extends Integration {

    private Chat chat;
    private Economy economy;
    private Permission permission;

    public VaultIntegration() {
        super("Vault");
        this.addRequiredClass("net.milkbowl.vault.economy.Economy");
    }

    @Override
    public void onEnable() {
        this.setupEconomy();
        this.setupPermissions();
        this.setupChat();
    }

    private boolean setupChat() {
        if (!isActive()) {
            return false;
        }
        try {
            RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
            if (rsp == null) {
                return false;
            }
            chat = rsp.getProvider();
            return chat != null;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setupEconomy() {
        if (!isActive()) {
            return false;
        }
        try {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) {
                return false;
            }
            economy = rsp.getProvider();
            return economy != null;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setupPermissions() {
        if (!isActive()) {
            return false;
        }
        try {
            RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
            if (rsp == null) {
                return false;
            }
            Permission perms = rsp.getProvider();
            permission = perms;
            return perms != null;
        } catch (
                Exception e) {
            return false;
        }

    }


    public Permission getPermission() {
        if (permission == null) {
            setupPermissions();
        }
        return permission;
    }

    public Economy getEconomy() {
        if (economy == null) {
            setupEconomy();
        }
        return economy;
    }

    public Chat getChat() {
        if (chat == null) {
            setupChat();
        }
        return chat;
    }

    public boolean withdrawIfEnough(OfflinePlayer player, double amount) {
        if (!isActive()) {
            return false;
        }
        final boolean hasEnough = getEconomy().getBalance(player) >= amount;
        if (hasEnough) {
            takeMoney(player, amount);
        }
        return hasEnough;
    }


    public boolean hasEnough(OfflinePlayer player, double amount) {
        if (!isActive()) {
            return false;
        }
        return getEconomy().getBalance(player) >= amount;
    }

    public void giveMoney(OfflinePlayer player, double amount) {
        if (!isActive()) {
            return;
        }
        getEconomy().depositPlayer(player, amount);
    }

    public void takeMoney(OfflinePlayer player, double amount) {
        if (!isActive()) {
            return;
        }
        getEconomy().withdrawPlayer(player, amount);
    }

    public double getBalance(OfflinePlayer player) {
        if (!isActive()) {
            return 0;
        }
        return getEconomy().getBalance(player);
    }


}
