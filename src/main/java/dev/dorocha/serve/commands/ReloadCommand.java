package dev.dorocha.serve.commands;

import dev.dorocha.serve.Serve;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final Serve plugin;

    public ReloadCommand(Serve plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("serve.reload")) {
            sender.sendMessage("§cYou do not have permission to use this command!");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.loadConfigValues();
            plugin.startServer();

            sender.sendMessage("§a[Serve] Plugin reloaded successfully!");
            return true;
        }

        sender.sendMessage("§eUsage: /serve reload");
        return true;
    }
}
