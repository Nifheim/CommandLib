package net.nifheim.bukkit.commandlib;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * @author Beelzebu
 */
public abstract class RegistrableCommand extends Command implements PluginIdentifiableCommand {

    private final Plugin plugin;
    private final boolean async;

    public RegistrableCommand(Plugin plugin, String command, String permission, boolean async, String... aliases) {
        super(command);
        this.plugin = plugin;
        if (permission != null) {
            setPermission(permission);
        }
        setAliases(new ArrayList<>(Arrays.asList(aliases)));
        this.async = async;
        CommandAPI.registerCommand(plugin, this);
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, String[] args) {
        if (getPermission() == null || sender.hasPermission(getPermission())) {
            if (async && Bukkit.isPrimaryThread()) {
                Bukkit.getAsyncScheduler().runNow(plugin, _ -> onCommand(sender, commandLabel, args));
            } else {
                onCommand(sender, commandLabel, args);
            }
        }
        return true;
    }

    /**
     * Method called when the command is executed.
     *
     * @param sender Who is executing this command.
     * @param label  /<parameter> being used to execute this command.
     * @param args   Arguments for this command.
     */
    public abstract void onCommand(CommandSender sender, String label, String[] args);
}
