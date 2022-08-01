package me.fullpage.manticlib.command;

import me.fullpage.manticlib.string.Txt;
import me.fullpage.manticlib.utils.ReflectionUtils;
import me.fullpage.manticlib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;

public abstract class ManticCommand extends Command implements PluginIdentifiableCommand {

    protected CommandSender sender;
    protected Player player = null;
    protected UUID uuid = null;
    protected final String permission;
    protected Integer minimumArgs = null;
    protected Integer maximumArgs = null;
    protected boolean canConsole = true;
    protected Plugin providingPlugin;
    protected String label;
    protected String[] args;

    public ManticCommand(String commandName, String permission) {
        super(commandName);
        this.permission = permission;
        this.providingPlugin = JavaPlugin.getProvidingPlugin(this.getClass());
    }

    public ManticCommand(String commandName) {
        super(commandName);
        this.permission = null;
        this.providingPlugin = JavaPlugin.getProvidingPlugin(this.getClass());
    }

    public abstract void run();

    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, Collection<String> args) {
        return execute(sender, commandLabel, args.toArray(new String[0]));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {


        this.label = commandLabel;
        this.args = args;
        this.sender = sender;

        if (isConsole() && !canConsole()) {
            sendOnlyPlayersMessage();
            return true;
        }

        if (!isConsole()) {
            player = (Player) sender;
            uuid = player.getUniqueId();
        }

        if (permission == null || sender.hasPermission(permission)/* || sender.isOp()*/) {
            if ((minimumArgs != null && args.length < minimumArgs) || (maximumArgs != null && args.length > maximumArgs)) {
                sendUsageMessage();
                return true;
            }
            run();
        } else {
            sendNoPermissionMessage();
        }

        return true;
    }

    @Nullable
    @Override
    public String getPermissionMessage() {
        return "&cYou do not have permission to use this command.";
    }

    protected void sendNoPermissionMessage() {
        sender.sendMessage(Txt.parse(this.getPermissionMessage()));
    }

    protected void sendOnlyPlayersMessage() {
        sender.sendMessage(Txt.parse("&cOnly players can run this command."));
    }

    public void sendMessage(CommandSender sender, String message) {
        if (Utils.isNullOrEmpty(message)) {
            return;
        }
        sender.sendMessage(Txt.parse(message));
    }


    public void sendMessage(String message) {
        if (Utils.isNullOrEmpty(message)) {
            return;
        }
        sender.sendMessage(Txt.parse(message));
    }
    public void sendMessage(String[] messages) {
       this.sendMessages(messages);
    }

    public void sendMessage(CommandSender sender, String message, Object... args) {
        if (Utils.isNullOrEmpty(message)) {
            return;
        }
        sender.sendMessage(Txt.parse(message, args));
    }

    public void sendMessage(String message, Object... args) {
        if (Utils.isNullOrEmpty(message)) {
            return;
        }
        sender.sendMessage(Txt.parse(message, args));
    }

    protected void sendMessages(String... messages) {
        Arrays.stream(messages)
                .forEach(message -> {
                    if (Txt.isNullOrEmpty(message)) return;
                    sender.sendMessage(Txt.parse(message));
                });
    }

    protected void sendMessages(CommandSender sender, String... messages) {
        Arrays.stream(messages)
                .forEach(message -> {
                    if (Txt.isNullOrEmpty(message)) return;
                    sender.sendMessage(Txt.parse(message));
                });
    }


    protected boolean isConsole() {
        return sender instanceof ConsoleCommandSender;
    }

    public void setCanConsole(boolean canConsole) {
        this.canConsole = canConsole;
    }

    public boolean canConsole() {
        return canConsole;
    }

    public void setMinimumArgs(Integer minimumArgs) {
        this.minimumArgs = minimumArgs;
    }

    public void setMaximumArgs(Integer maximumArgs) {
        this.maximumArgs = maximumArgs;
    }

    public void sendUsageMessage() {
        sendMessage("&cIncorrect usage, please try:");
        String usage = (this.getUsage() == null ? "&7<Cannot get usage>" : this.getUsage());
        sendMessage("&e" + usage.replace("<command>", label));
    }

    @Override
    public String getUsage() {
        String usage = super.getUsage();
        return usage == null ? "/<command>" : usage;
    }

    @NotNull
    public Command setAliases(@NotNull String... aliases) {
        return super.setAliases(Txt.list(aliases));
    }

    @Nullable
    @Override
    public String getPermission() {
        return permission;
    }

    @NotNull
    @Override
    public Plugin getPlugin() {
        return providingPlugin;
    }

    public String[] getArgs() {
        return args;
    }

    public String getArgs(int index) {
        return args[index];
    }

    private static final HashMap<Plugin, Set<ManticCommand>> registeredCommands = new HashMap<>();

    public void register() {
        getSimpleCommandMap().register(providingPlugin.getDescription().getName(), this);
        final Set<ManticCommand> manticCommandSet = registeredCommands.getOrDefault(providingPlugin, null);
        if (manticCommandSet == null) {
            registeredCommands.put(providingPlugin, Txt.set(this));
        } else {
            manticCommandSet.add(this);
        }
    }

    public static void register(ManticCommand... command) {
        if (command == null || command.length == 0) return;
        for (ManticCommand manticCommand : command) {
            manticCommand.register();
        }
    }

    public void unregister() {
        final SimpleCommandMap simpleCommandMap = getSimpleCommandMap();
        this.unregister(simpleCommandMap);
        final Map<String, Command> knownCommands = getSimpleCommandMapDotKnownCommands(simpleCommandMap);
        Iterator<Map.Entry<String, Command>> iter = knownCommands.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Command> entry = iter.next();
            String name = entry.getKey();
            Command command = entry.getValue();

            ManticCommand fc = getManticCommand(command);
            if (fc == null) continue;
            if (fc != fc) {
                continue;
            }
            command.unregister(simpleCommandMap);
            iter.remove();
        }

    }

    public static ManticCommand getManticCommand(Command command) {
        if (command == null) return null;
        if (!(command instanceof ManticCommand)) return null;
        return (ManticCommand) command;
    }

    public static Set<ManticCommand> getCommands(Plugin plugin) {
        if (plugin == null) return new HashSet<>();
        return registeredCommands.getOrDefault(plugin, new HashSet<>());
    }

    protected static Field SERVER_DOT_COMMAND_MAP = ReflectionUtils.getField(Bukkit.getServer().getClass(), "commandMap");

    public static SimpleCommandMap getSimpleCommandMap() {
        Server server = Bukkit.getServer();
        return ReflectionUtils.getField(SERVER_DOT_COMMAND_MAP, server);
    }

    protected static Field SIMPLE_COMMAND_MAP_DOT_KNOWN_COMMANDS = ReflectionUtils.getField(SimpleCommandMap.class, "knownCommands");

    public static Map<String, Command> getSimpleCommandMapDotKnownCommands(SimpleCommandMap simpleCommandMap) {
        return ReflectionUtils.getField(SIMPLE_COMMAND_MAP_DOT_KNOWN_COMMANDS, simpleCommandMap);
    }


}