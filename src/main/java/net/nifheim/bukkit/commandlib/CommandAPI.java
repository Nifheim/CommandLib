package net.nifheim.bukkit.commandlib;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

/**
 * @author Beelzebu
 */
@SuppressWarnings("unchecked")
public class CommandAPI {

    private static final Map<Plugin, List<Command>> REGISTERED_COMMANDS = new HashMap<>();
    private static CommandMap commandMap;
    private static Field knownCommandsField;

    static {
        try {
            Field commandMapField = SimplePluginManager.class.getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getPluginManager());
            try {
                knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
                knownCommandsField.setAccessible(true);
            } catch (ReflectiveOperationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to get known commands map: ", ex);
            }
        } catch (ReflectiveOperationException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to get command map: ", ex);
        }
    }

    private CommandAPI() {
    }

    public static void unregister(Plugin plugin) {
        Iterator<Command> commandIterator = REGISTERED_COMMANDS.get(plugin).iterator();
        while (commandIterator.hasNext()) {
            Command command = commandIterator.next();
            unregisterCommand(plugin, command);
            commandIterator.remove();
        }
    }

    public static void registerCommand(Plugin plugin, Command command) {
        REGISTERED_COMMANDS.computeIfAbsent(plugin, p -> new ArrayList<>()).add(command);
        getCommandMap().register(plugin.getName(), command);
    }

    public static void unregisterCommand(Plugin plugin, Command command) {
        if (command == null) {
            return;
        }
        if (REGISTERED_COMMANDS.get(plugin) != null && REGISTERED_COMMANDS.get(plugin).contains(command)) {
            command.unregister(getCommandMap());
            Command byLabel = getCommandMap().getCommand(command.getLabel());
            if (byLabel != null) {
                byLabel.unregister(getCommandMap());
                getKnownCommands().remove(command.getLabel().toLowerCase(Locale.ROOT));
            }
            Command byName = getCommandMap().getCommand(command.getName());
            if (byName != null) {
                byName.unregister(getCommandMap());
                getKnownCommands().remove(command.getName().toLowerCase(Locale.ROOT));
            }
            Command byPrefix = getCommandMap().getCommand(getCommand(plugin, command.getName()));
            if (byPrefix != null) {
                byPrefix.unregister(getCommandMap());
                getKnownCommands().remove(getCommand(plugin, command.getName()));
            }
            Iterator<String> aliases = command.getAliases().iterator();
            while (aliases.hasNext()) {
                String alias = aliases.next();
                Command aliasByName = getCommandMap().getCommand(alias);
                if (aliasByName != null) {
                    aliasByName.unregister(getCommandMap());
                }
                Command aliasByPrefix = getCommandMap().getCommand(getCommand(plugin, alias));
                if (aliasByPrefix != null) {
                    aliasByPrefix.unregister(getCommandMap());
                }
                getKnownCommands().remove(alias.toLowerCase(Locale.ROOT));
                getKnownCommands().remove(getCommand(plugin, alias));
                aliases.remove();
            }
        }
    }

    private static String getCommand(Plugin plugin, String command) {
        return (plugin.getName() + ':' + command).trim().toLowerCase(Locale.ROOT);
    }

    public static CommandMap getCommandMap() {
        return commandMap;
    }

    public static Map<String, Command> getKnownCommands() {
        try {
            return (Map<String, Command>) knownCommandsField.get(commandMap);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}

