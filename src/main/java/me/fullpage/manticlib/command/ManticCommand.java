package me.fullpage.manticlib.command;

import me.fullpage.manticlib.ManticLib;
import me.fullpage.manticlib.ManticPlugin;
import me.fullpage.manticlib.string.Txt;
import me.fullpage.manticlib.utils.ReflectionUtils;
import me.fullpage.manticlib.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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
        this.providingPlugin = ManticPlugin.getProvidingPlugin(this.getClass());
    }

    public ManticCommand(String commandName) {
        this(commandName, null);
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

        if (isConsole()) {
            player = null;
            uuid = null;
        } else {
            player = (Player) sender;
            uuid = player.getUniqueId();
        }

        if (permission == null || sender.hasPermission(permission)) {
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
        return ManticLib.get().getConfiguration().defaultNoPermissionCommand;
    }

    protected void sendNoPermissionMessage() {
        sender.sendMessage(Txt.parse(this.getPermissionMessage()));
    }

    protected void sendOnlyPlayersMessage() {
        sender.sendMessage(Txt.parse(ManticLib.get().getConfiguration().defaultOnlyPlayersCommand));
    }

    public void sendMessage(CommandSender sender, String message) {
        if (Utils.isNullOrEmpty(message)) {
            return;
        }
        String parse = Txt.parse(message);
        if (parse.contains("<command>")) {
            parse = parse.replace("<command>", label);
        }
        sender.sendMessage(parse);
    }


    public void sendMessage(String message) {
        this.sendMessage(sender, message);
    }

    public void sendMessage(String[] messages) {
        this.sendMessages(messages);
    }

    public void sendMessage(CommandSender sender, String message, Object... args) {
        if (Utils.isNullOrEmpty(message)) {
            return;
        }
        String parse = Txt.parse(message, args);
        if (parse.contains("<command>")) {
            parse = parse.replace("<command>", label);
        }
        sender.sendMessage(parse);
    }

    public void sendMessage(String message, Object... args) {
        if (Utils.isNullOrEmpty(message)) {
            return;
        }
        String parse = Txt.parse(message, args);
        if (parse.contains("<command>")) {
            parse = parse.replace("<command>", label);
        }
        sender.sendMessage(parse);
    }

    protected void sendMessages(String... messages) {
        Arrays.stream(messages)
                .forEach(message -> {
                    if (Txt.isNullOrEmpty(message)) return;
                    String parse = Txt.parse(message);
                    if (parse.contains("<command>")) {
                        parse = parse.replace("<command>", label);
                    }
                    sender.sendMessage(parse);
                });
    }

    protected void sendMessages(CommandSender sender, String... messages) {
        Arrays.stream(messages)
                .forEach(message -> {
                    if (Txt.isNullOrEmpty(message)) return;
                    sender.sendMessage(Txt.parse(message));
                });
    }

    public Optional<Player> getPlayer(String string) {
        if (Utils.isNullOrEmpty(string)) {
            return Optional.empty();
        }
        return Optional.ofNullable(Bukkit.getPlayer(string));
    }

    public Optional<OfflinePlayer> getOfflinePlayer(String name) {
        return getOfflinePlayer(name, true);
    }


    public Optional<OfflinePlayer> getOfflinePlayer(int index) {
        return getOfflinePlayer(index, true);
    }

    public Player getArgAsPlayer(int index) {
        return getPlayer(index).orElse(null);
    }

    public OfflinePlayer getArgAsOfflinePlayer(int index) {
        return getOfflinePlayer(index).orElse(null);
    }

    public Optional<Player> getPlayer(int index) {
        if (index < 0 || index >= args.length) {
            return Optional.empty();
        }
        return getPlayer(args[index]);
    }

    public Optional<OfflinePlayer> getOfflinePlayer(int index, boolean hasBeenOnBefore) {
        if (index < 0 || index >= args.length) {
            return Optional.empty();
        }
        return getOfflinePlayer(args[index], hasBeenOnBefore);
    }

    public Optional<OfflinePlayer> getOfflinePlayer(String name, boolean hasBeenOnBefore) {
        if (Utils.isNullOrEmpty(name)) {
            return Optional.empty();
        }

        OfflinePlayer offlinePlayer;

        if (Utils.isUuid(name)) {
            offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(name));
        } else {
            offlinePlayer = Bukkit.getOfflinePlayer(name);
        }

        if (hasBeenOnBefore) {
            return (offlinePlayer.hasPlayedBefore() || offlinePlayer.isOnline()) ? Optional.of(offlinePlayer) : Optional.empty();
        }

        return Optional.of(offlinePlayer);
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
        String internal = this.getUsage();
        String usage = (internal == null ? "&7<Cannot get usage>" : internal);
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
        if (index < 0 || index >= args.length) {
            return null;
        }
        return args[index];
    }

    public Optional<String> getArg(int index) {
        return Optional.ofNullable(this.getArgs(index));
    }

    private static final HashMap<Plugin, Set<ManticCommand>> registeredCommands = new HashMap<>();

    public void register() {
        getSimpleCommandMap().register(providingPlugin.getDescription().getName(), this);
        Set<ManticCommand> manticCommandSet = registeredCommands.getOrDefault(providingPlugin, null);
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
        SimpleCommandMap simpleCommandMap = getSimpleCommandMap();
        this.unregister(simpleCommandMap);
        Map<String, Command> knownCommands = getSimpleCommandMapDotKnownCommands(simpleCommandMap);
        Iterator<Map.Entry<String, Command>> iter = knownCommands.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Command> entry = iter.next();
            Command command = entry.getValue();

            ManticCommand manticCommand = getManticCommand(command);
            if (manticCommand == null) {
                continue;
            }

            if (!command.getLabel().equals(this.getLabel())) {
                continue;
            }

            command.unregister(simpleCommandMap);
            iter.remove();
        }

    }

    public void reload() {
        try {
            this.unregister();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.register();
        }
    }

    public ManticCommand updateCommandLabel(String label) {
        this.label = label;
        super.setLabel(label);
        return this;
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