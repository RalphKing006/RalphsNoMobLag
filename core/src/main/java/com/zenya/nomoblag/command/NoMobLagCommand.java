package com.zenya.nomoblag.command;

import com.zenya.nomoblag.NoMobLag;
import com.zenya.nomoblag.file.ConfigManager;
import com.zenya.nomoblag.file.MessagesManager;
import com.zenya.nomoblag.scheduler.TrackTPSTask;
import com.zenya.nomoblag.util.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class NoMobLagCommand implements CommandExecutor {

    private void sendUsage(CommandSender sender) {
        ChatUtils.sendMessage(sender, "&8&m*]----------[*&r &bNoMobLag &8&m*]----------[*&r");
        ChatUtils.sendMessage(sender, "&b/nomoblag help&f -&9 Shows this help page");
        ChatUtils.sendMessage(sender, "&b/nomoblag stats&f -&9 Shows server entity statistics");
        ChatUtils.sendMessage(sender, "&b/nomoblag reload&f -&9 Reloads the plugin\'s config and messages");
        ChatUtils.sendMessage(sender, "&b/nomoblag freeze [chunk/world/all]&f -&9 Removes AI from all specified mobs");
        ChatUtils.sendMessage(sender, "&b/nomoblag unfreeze [chunk/world/all]&f -&9 Returns AI to all specified mobs");
        ChatUtils.sendMessage(sender, "&b/nomoblag setcollisions [true/false]&f -&9 Toggles collision physics for all entities");
        ChatUtils.sendMessage(sender, "&b/nomoblag loadspawners&f -&9 Imposes spawner limits in config to all loaded spawners");
        ChatUtils.sendMessage(sender, "&8&m*]--------------------------------[*&r");
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
        //No command arguments
        if(args.length < 1) {
            sendUsage(sender);
            return true;
        }

        //No permission
        if(!sender.hasPermission("nochunklag.command." + args[0])) {
            ChatUtils.sendMessage(sender, "&4You do not have permission to use this command");
            return true;
        }

        //help, stats, reload, loadspawners
        if(args.length == 1) {
            if(args[0].toLowerCase().equals("help")) {
                sendUsage(sender);
                return true;
            }

            if(args[0].toLowerCase().equals("stats")) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        int totalMobs = 0;
                        int frozenMobs = 0;
                        int loadedSpawners = 0;
                        int loadedChunks = 0;
                        float tps = TrackTPSTask.getInstance().getAverageTps();
                        int playercount = Bukkit.getOnlinePlayers().size();
                        int tpsSpawnChance = ConfigManager.getInstance().getInt("mob-spawning.spawn-chance-at-tps." + String.valueOf((int) tps));
                        int playerSpawnChance = 100;
                        ArrayList<String> keyList = ConfigManager.getInstance().getKeys("mob-spawning.spawn-chance-at-playercount");
                        if(keyList != null && keyList.size() != 0) {
                            int smallestDiff = Math.abs(Integer.valueOf(keyList.get(0)) - playercount);
                            int smallestIndex = 0;
                            for (int i = 1; i < keyList.size(); i++) {
                                int difference = Math.abs(Integer.valueOf(keyList.get(i)) - playercount);
                                if (difference < smallestDiff) {
                                    smallestDiff = difference;
                                    smallestIndex = i;
                                }
                            }
                            playerSpawnChance = ConfigManager.getInstance().getInt("mob-spawning.spawn-chance-at-playercount." + String.valueOf(keyList.get(smallestIndex)));
                        }
                        float finalSpawnChance = tpsSpawnChance * playerSpawnChance / 100.0f;

                        for(World world : Bukkit.getServer().getWorlds()) {
                            for(Chunk chunk : world.getLoadedChunks()) {
                                loadedChunks++;
                                for(BlockState state : chunk.getTileEntities()) {
                                    if(state instanceof CreatureSpawner) {
                                        loadedSpawners++;
                                    }
                                }
                            }
                            for(Entity ent : world.getEntities()) {
                                if(ent instanceof LivingEntity) {
                                    LivingEntity entity = (LivingEntity) ent;
                                    totalMobs++;
                                    if(!entity.hasAI()) {
                                        frozenMobs++;
                                    }
                                }
                            }
                        }

                        ChatUtils.sendMessage(sender, "&8&m*]----------[*&r &bNoMobLag Statistics &8&m*]----------[*&r");
                        ChatUtils.sendMessage(sender, "&bTotal Mobs: &9" + String.valueOf(totalMobs) + " (" + String.valueOf((int) (100*frozenMobs/totalMobs)) + "% Frozen)");
                        ChatUtils.sendMessage(sender, "&bLoaded Spawners: &9" + String.valueOf(loadedSpawners));
                        ChatUtils.sendMessage(sender, "&bLoaded Chunks: &9" + String.valueOf(loadedChunks));
                        ChatUtils.sendMessage(sender, "&bServer TPS: &9" + String.valueOf(tps));
                        ChatUtils.sendMessage(sender, "&bPlayercount: &9" + String.valueOf(playercount));
                        ChatUtils.sendMessage(sender, "&bSpawn Rate: &9" + String.valueOf(finalSpawnChance) + "%");
                        ChatUtils.sendMessage(sender, "&8&m*]----------------------------------------[*&r");
                    }
                }.runTask(NoMobLag.getInstance());
                return true;
            }

            if(args[0].toLowerCase().equals("reload")) {
                ConfigManager.reloadConfig();
                MessagesManager.reloadMessages();
                ChatUtils.sendMessage(sender, "&aNoMobLag has been reloaded");
                return true;
            }

            if(args[0].toLowerCase().equals("loadspawners")) {
                new BukkitRunnable() {
                    int spawners = 0;
                    @Override
                    public void run() {
                        for(World world : Bukkit.getServer().getWorlds()) {
                            for(Chunk chunk : world.getLoadedChunks()) {
                                for(BlockState state : chunk.getTileEntities()) {
                                    if(state instanceof CreatureSpawner) {
                                        CreatureSpawner spawner = (CreatureSpawner) state;
                                        //Handle spawner activation range
                                        spawner.setSpawnRange(4);
                                        spawner.setRequiredPlayerRange(ConfigManager.getInstance().getInt("spawners.activation-range"));

                                        //Handle max mobs per minute
                                        spawner.setSpawnCount(1);
                                        spawner.setMinSpawnDelay(Integer.valueOf(60*20/ConfigManager.getInstance().getInt("spawners.max-mobs-per-minute")));
                                        spawner.setMaxSpawnDelay(Integer.valueOf(60*20/ConfigManager.getInstance().getInt("spawners.max-mobs-per-minute")));
                                        spawners++;
                                    }
                                }
                            }
                        }
                        ChatUtils.sendMessage(sender, "&dSpawner limits have been imposed on &5" + String.valueOf(spawners) + " &dspawners");
                    }
                }.runTask(NoMobLag.getInstance());
                return true;
            }

            //Wrong arg1
            sendUsage(sender);
            return true;
        }

        //freeze, unfreeze, setcollisions
        if(args.length == 2) {
            if(args[0].toLowerCase().equals("freeze")) {

                if (args[1].toLowerCase().equals("chunk")) {
                    if (!(sender instanceof Player)) {
                        ChatUtils.sendMessage(sender, "&4This command can only be used by a player");
                        return true;
                    }
                    Player player = (Player) sender;
                    new BukkitRunnable() {
                        int frozen = 0;

                        @Override
                        public void run() {
                            for (Entity ent : player.getLocation().getChunk().getEntities()) {
                                if (ent instanceof LivingEntity) {
                                    LivingEntity entity = (LivingEntity) ent;
                                    ArrayList<String> freezeBypassMobs = ConfigManager.getInstance().getList("mob-freezing.freeze-bypass-mobs");
                                    if((freezeBypassMobs == null) || (freezeBypassMobs.size() == 0) || !(freezeBypassMobs.contains(entity.getType().name()))) {
                                        if(entity.hasAI()) {
                                            entity.setAI(false);
                                            frozen++;
                                        }
                                    }
                                }
                            }
                            ChatUtils.sendMessage(player, "&4" + String.valueOf(frozen) + " &cmobs have been frozen");
                        }
                    }.runTaskAsynchronously(NoMobLag.getInstance());
                    return true;
                }

                if (args[1].toLowerCase().equals("world")) {
                    if (!(sender instanceof Player)) {
                        ChatUtils.sendMessage(sender, "&4This command can only be used by a player");
                        return true;
                    }
                    Player player = (Player) sender;
                    new BukkitRunnable() {
                        int frozen = 0;

                        @Override
                        public void run() {
                            for (Entity ent : player.getWorld().getEntities()) {
                                if (ent instanceof LivingEntity) {
                                    LivingEntity entity = (LivingEntity) ent;
                                    ArrayList<String> freezeBypassMobs = ConfigManager.getInstance().getList("mob-freezing.freeze-bypass-mobs");
                                    if((freezeBypassMobs == null) || (freezeBypassMobs.size() == 0) || !(freezeBypassMobs.contains(entity.getType().name()))) {
                                        if(entity.hasAI()) {
                                            entity.setAI(false);
                                            frozen++;
                                        }
                                    }
                                }
                            }
                            ChatUtils.sendMessage(player, "&4" + String.valueOf(frozen) + " &cmobs have been frozen");
                        }
                    }.runTaskAsynchronously(NoMobLag.getInstance());
                    return true;
                }

                if (args[1].toLowerCase().equals("all")) {
                    new BukkitRunnable() {
                        int frozen = 0;

                        @Override
                        public void run() {
                            for (World world : Bukkit.getServer().getWorlds()) {
                                for (Entity ent : world.getEntities()) {
                                    if (ent instanceof LivingEntity) {
                                        LivingEntity entity = (LivingEntity) ent;
                                        ArrayList<String> freezeBypassMobs = ConfigManager.getInstance().getList("mob-freezing.freeze-bypass-mobs");
                                        if((freezeBypassMobs == null) || (freezeBypassMobs.size() == 0) || !(freezeBypassMobs.contains(entity.getType().name()))) {
                                            if(entity.hasAI()) {
                                                entity.setAI(false);
                                                frozen++;
                                            }
                                        }
                                    }
                                }
                            }
                            ChatUtils.sendMessage(sender, "&4" + String.valueOf(frozen) + " &cmobs have been frozen");
                        }
                    }.runTaskAsynchronously(NoMobLag.getInstance());
                    return true;
                }
                //Wrong arg2 for freeze
                sendUsage(sender);
                return true;
            }

            if(args[0].toLowerCase().equals("unfreeze")) {

                if(args[1].toLowerCase().equals("chunk")) {
                    if(!(sender instanceof Player)) {
                        ChatUtils.sendMessage(sender, "&4This command can only be used by a player");
                        return true;
                    }
                    Player player = (Player) sender;
                    new BukkitRunnable() {
                        int unfrozen = 0;
                        @Override
                        public void run() {
                            for (Entity ent : player.getLocation().getChunk().getEntities()) {
                                if(ent instanceof LivingEntity) {
                                    LivingEntity entity = (LivingEntity) ent;
                                    if(!entity.hasAI()) {
                                        entity.setAI(true);
                                        unfrozen++;
                                    }
                                }
                            }
                            ChatUtils.sendMessage(player, "&2" + String.valueOf(unfrozen) + " &amobs have been unfrozen");
                        }
                    }.runTaskAsynchronously(NoMobLag.getInstance());
                    return true;
                }

                if(args[1].toLowerCase().equals("world")) {
                    if(!(sender instanceof Player)) {
                        ChatUtils.sendMessage(sender, "&4This command can only be used by a player");
                        return true;
                    }
                    Player player = (Player) sender;
                    new BukkitRunnable() {
                        int unfrozen = 0;
                        @Override
                        public void run() {
                            for (Entity ent : player.getWorld().getEntities()) {
                                if(ent instanceof LivingEntity) {
                                    LivingEntity entity = (LivingEntity) ent;
                                    if(!entity.hasAI()) {
                                        entity.setAI(true);
                                        unfrozen++;
                                    }
                                }
                            }
                            ChatUtils.sendMessage(player, "&2" + String.valueOf(unfrozen) + " &amobs have been unfrozen");
                        }
                    }.runTaskAsynchronously(NoMobLag.getInstance());
                    return true;
                }

                if(args[1].toLowerCase().equals("all")) {
                    new BukkitRunnable() {
                        int unfrozen = 0;
                        @Override
                        public void run() {
                            for (World world : Bukkit.getServer().getWorlds()) {
                                for (Entity ent : world.getEntities()) {
                                    if(ent instanceof LivingEntity) {
                                        LivingEntity entity = (LivingEntity) ent;
                                        if(!entity.hasAI()) {
                                            entity.setAI(true);
                                            unfrozen++;
                                        }
                                    }
                                }
                            }
                            ChatUtils.sendMessage(sender, "&2" + String.valueOf(unfrozen) + " &amobs have been unfrozen");
                        }
                    }.runTaskAsynchronously(NoMobLag.getInstance());
                    return true;
                }
                //Wrong arg2 for unfreeze
                sendUsage(sender);
                return true;
            }

            if(args[0].toLowerCase().equals("setcollisions")) {

                if(args[1].toLowerCase().equals("true")) {
                    new BukkitRunnable() {
                        int collidable = 0;
                        @Override
                        public void run() {
                            try {
                                for (World world : Bukkit.getServer().getWorlds()) {
                                    for (Entity ent : world.getEntities()) {
                                        if(ent instanceof LivingEntity) {
                                            LivingEntity entity = (LivingEntity) ent;
                                            if(!entity.isCollidable()) {
                                                entity.setCollidable(true);
                                                collidable++;
                                            }
                                        }
                                    }
                                }
                            } catch(NoSuchMethodError exc) {
                                //Silence 1.8 errors
                            }
                            ChatUtils.sendMessage(sender, "&aEnabled collision physics for &2" + String.valueOf(collidable) + " &aentities");
                        }
                    }.runTaskAsynchronously(NoMobLag.getInstance());
                    return true;
                }

                if(args[1].toLowerCase().equals("false")) {
                    new BukkitRunnable() {
                        int collidable = 0;

                        @Override
                        public void run() {
                            try {
                                for (World world : Bukkit.getServer().getWorlds()) {
                                    for (Entity ent : world.getEntities()) {
                                        if (ent instanceof LivingEntity) {
                                            LivingEntity entity = (LivingEntity) ent;
                                            if(entity.isCollidable()) {
                                                entity.setCollidable(false);
                                                collidable++;
                                            }
                                        }
                                    }
                                }
                            } catch(NoSuchMethodError exc) {
                                //Silence 1.8 errors
                            }
                            ChatUtils.sendMessage(sender, "&cDisabled collision physics for &4" + String.valueOf(collidable) + " &centities");
                        }
                    }.runTaskAsynchronously(NoMobLag.getInstance());
                    return true;
                }

                //Wrong arg2 for setcollisions
                sendUsage(sender);
                return true;
            }

            //Wrong arg1
            sendUsage(sender);
            return true;
        }
        //Incorrect number of args
        sendUsage(sender);
        return true;
    }
}
