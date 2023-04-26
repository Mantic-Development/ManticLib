package me.fullpage.manticlib.integrations;

import me.fullpage.manticlib.integrations.manager.Integration;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collection;
import java.util.HashMap;

import static org.bukkit.Bukkit.getServer;

public class VaultIntegration extends Integration {

    private Chat chat;
    private Economy economy;
    private HashMap<String, Economy> economies;
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

    public boolean setupEconomy() {
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

    public boolean setupEconomy(String economy) {
        if (!isActive()) {
            return false;
        }
        if (economies == null) {
            economies = new HashMap<>();
        }
        try {

            Collection<RegisteredServiceProvider<Economy>> registrations = getServer().getServicesManager().getRegistrations(Economy.class);
            Economy provider = null;
            for (RegisteredServiceProvider<Economy> registration : registrations) {
                if (registration == null) {
                    continue;
                }
                if (registration.getPlugin().getName().equals(economy) || registration.getProvider().getClass().getName().equals(economy)) {
                    provider = registration.getProvider();
                    break;
                }

            }

            if (provider == null) {
                return false;
            }

            economies.put(economy, provider);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
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

    /**
     * @apiNote VaultIntegration#setupEconomy(String) must be called once before this method is used
     */
    public boolean withdrawIfEnough(OfflinePlayer player, double amount, String economy) {
        if (!isActive()) {
            return false;
        }
        if (economies == null) {
            setupEconomy(economy);
            return false;
        }
        Economy economy1 = economies.get(economy);
        if (economy1 == null) {
            setupEconomy(economy);
            return false;
        }
        final boolean hasEnough = economy1.getBalance(player) >= amount;
        if (hasEnough) {
            takeMoney(player, amount, economy);
        }
        return hasEnough;
    }


    public boolean hasEnough(OfflinePlayer player, double amount) {
        if (!isActive()) {
            return false;
        }
        return getEconomy().getBalance(player) >= amount;
    }


    /**
     * @apiNote VaultIntegration#setupEconomy(String) must be called once before this method is used
     */
    public boolean hasEnough(OfflinePlayer player, double amount, String economy) {
        if (!isActive()) {
            return false;
        }
        if (economies == null) {
            setupEconomy(economy);
            return false;
        }
        Economy economy1 = economies.get(economy);
        if (economy1 == null) {
            setupEconomy(economy);
            return false;
        }
        return economy1.getBalance(player) >= amount;
    }

    public void giveMoney(OfflinePlayer player, double amount) {
        if (!isActive()) {
            return;
        }
        getEconomy().depositPlayer(player, amount);
    }


    /**
     * @apiNote VaultIntegration#setupEconomy(String) must be called once before this method is used
     */
    public void giveMoney(OfflinePlayer player, double amount, String economy) {
        if (!isActive()) {
            return;
        }
        if (economies == null) {
            setupEconomy(economy);
            return;
        }
        Economy economy1 = economies.get(economy);
        if (economy1 == null) {
            setupEconomy(economy);
            return;
        }
        economy1.depositPlayer(player, amount);
    }

    public void takeMoney(OfflinePlayer player, double amount) {
        if (!isActive()) {
            return;
        }
        getEconomy().withdrawPlayer(player, amount);
    }


    /**
     * @apiNote VaultIntegration#setupEconomy(String) must be called once before this method is used
     */
    public void takeMoney(OfflinePlayer player, double amount, String economy) {
        if (!isActive()) {
            return;
        }
        if (economies == null) {
            setupEconomy(economy);
            return;
        }
        Economy economy1 = economies.get(economy);
        if (economy1 == null) {
            setupEconomy(economy);
            return;
        }
        economy1.withdrawPlayer(player, amount);
    }

    public double getBalance(OfflinePlayer player) {
        if (!isActive()) {
            return 0;
        }
        return getEconomy().getBalance(player);
    }

    public double getBalance(OfflinePlayer player, String economy) {
        if (!isActive()) {
            return 0;
        }
        if (economies == null) {
            setupEconomy(economy);
            return 0;
        }
        Economy economy1 = economies.get(economy);
        if (economy1 == null) {
            setupEconomy(economy);
            return 0;
        }
        return economy1.getBalance(player);
    }


}
