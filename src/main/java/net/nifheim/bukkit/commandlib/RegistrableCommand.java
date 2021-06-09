package net.nifheim.bukkit.commandlib;

import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * @author Beelzebu
 */
public abstract class RegistrableCommand extends Command {

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
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (getPermission() == null || sender.hasPermission(getPermission())) {
            if (async && Bukkit.isPrimaryThread()) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> onCommand(sender, args));
            } else {
                onCommand(sender, args);
            }
        } else {
            if (getPermissionMessage() != null) {
                sender.sendMessage(getPermissionMessage());
            }
        }
        return true;
    }

    public abstract void onCommand(CommandSender sender, String[] args);
}
