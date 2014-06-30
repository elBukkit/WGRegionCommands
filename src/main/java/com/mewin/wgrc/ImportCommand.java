package com.mewin.wgrc;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class ImportCommand implements CommandExecutor {
    private final Plugin plugin;

    public ImportCommand(Plugin plugin) {
        this.plugin = plugin;
    }

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        WorldGuardPlugin worldGuard = (WorldGuardPlugin)plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        Map<String, ProtectedRegion> regions = new HashMap<String, ProtectedRegion>();
        Collection<World> worlds = plugin.getServer().getWorlds();
        for (World world : worlds) {
            RegionManager manager = worldGuard.getRegionManager(world);
            if (manager != null) {
                regions.putAll(manager.getRegions());
            }
        }

        String filename = "regioncommands.yml";
        if (args.length > 0) {
            filename = args[0];
        }

        File file = new File(plugin.getDataFolder(), filename);
        if (file.exists()) {
            try {
                YamlConfiguration config = new YamlConfiguration();
                config.load(file);
                Collection<String> regionKeys = config.getKeys(false);
                int added = 0;
                int updated = 0;
                int errors = 0;
                for (String regionKey : regionKeys) {
                    ProtectedRegion region = regions.get(regionKey);
                    if (region == null) {
                        errors++;
                    } else {
                        Set<String> currentCommands = (Set<String>)region.getFlag(WGRegionCommandsPlugin.SERVER_COMMAND_ENTER_FLAG);
                        if (currentCommands == null || currentCommands.size() == 0) {
                            added++;
                        } else {
                            updated++;
                            currentCommands.clear();
                        }
                        Set<String> newCommands = new HashSet<String>(config.getStringList(regionKey));
                        region.setFlag(WGRegionCommandsPlugin.SERVER_COMMAND_ENTER_FLAG, newCommands);
                    }
                }

                sender.sendMessage("Imported file: " + file.getName() + ", updated: "
                        + updated + ", added: " + added + ", not found: " + errors);
            } catch (Exception ex) {
                ex.printStackTrace();
                sender.sendMessage("Error importing file: " + file.getName());
            }
        } else {
            sender.sendMessage("File not found: " + file.getAbsolutePath());
        }
		return true;
	}
}
