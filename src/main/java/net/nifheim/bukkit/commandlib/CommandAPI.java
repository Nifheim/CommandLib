package net.nifheim.bukkit.commandlib;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 */
public class CommandAPI {

    private static final Map<Plugin, List<Command>> REGISTERED_COMMANDS = new HashMap<>();
    private static CommandMap commandMap;

    static {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Failed to get command map: ", ex);
        }
    }

    private CommandAPI() {
    }

    public static void unregister(Plugin plugin) {
        Iterator<Command> commandIterator = REGISTERED_COMMANDS.get(plugin).iterator();
        commandIterator.forEachRemaining(command -> unregisterCommand(plugin, command));
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
        command.setLabel(command.getName());
    }

    private static CommandMap getCommandMap() {
        return commandMap;
    }
}

