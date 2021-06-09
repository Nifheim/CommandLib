package net.nifheim.bukkit.commandlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 * TODO: add support for 1.8+
 */
public class CommandAPI {

    private static final Map<Plugin, List<Command>> REGISTERED_COMMANDS = new HashMap<>();
    private static CommandMap commandMap;

    private CommandAPI() {
    }

    public static void unregister(Plugin plugin) {
        Iterator<Command> commandIterator = REGISTERED_COMMANDS.get(plugin).iterator();
        commandIterator.forEachRemaining(command -> unregisterCommand(plugin, command));
        Iterator<Map.Entry<String, Command>> knownCommandsIterator = new HashSet<>(getCommandMap().getKnownCommands().entrySet()).iterator();
        knownCommandsIterator.forEachRemaining(entry -> {
            String name = entry.getKey();
            Command command = entry.getValue();
            if (name.startsWith(plugin.getName().toLowerCase(Locale.ENGLISH))) {
                unregisterCommand(plugin, command);
            }
        });
    }

    public static void registerCommand(Plugin plugin, Command command) {
        REGISTERED_COMMANDS.computeIfAbsent(plugin, p -> new ArrayList<>()).add(command);
        unregisterCommand(plugin, command);
        getCommandMap().register(plugin.getName(), command);
    }

    public static void unregisterCommand(Plugin plugin, Command command) {
        if (command == null) {
            return;
        }
        REGISTERED_COMMANDS.get(plugin).remove(command);
        command.unregister(getCommandMap());
        Command bukkitCommand = getCommandMap().getCommand(command.getName());
        if (bukkitCommand != null) {
            bukkitCommand.unregister(getCommandMap());
        }
        Map<String, Command> knownCommands = getKnownCommandsMap();
        Set<String> toRemove = new HashSet<>();
        command.setLabel(command.getName());
        for (Map.Entry<String, Command> entry : knownCommands.entrySet()) {
            String name = entry.getKey();
            Command registeredCommand = entry.getValue();
            if (name.equalsIgnoreCase(command.getName()) || name.startsWith(plugin.getName().toLowerCase(Locale.ROOT))) {
                registeredCommand.unregister(getCommandMap());
                toRemove.add(name);
            }
        }
        toRemove.forEach(getKnownCommandsMap()::remove);
        command.getAliases().forEach(getKnownCommandsMap()::remove);
        getKnownCommandsMap().remove(plugin.getName() + ":" + command.getName().toLowerCase(Locale.ENGLISH).trim());
    }

    private static CommandMap getCommandMap() {
        if (commandMap != null) {
            return commandMap;
        }
        return commandMap = Bukkit.getCommandMap();
    }


    @SuppressWarnings("unchecked")
    private static Map<String, Command> getKnownCommandsMap() {
        return getCommandMap().getKnownCommands();
    }
}

