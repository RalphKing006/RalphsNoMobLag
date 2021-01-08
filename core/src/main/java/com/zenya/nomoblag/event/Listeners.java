package com.zenya.nomoblag.event;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.zenya.nomoblag.NoMobLag;
import com.zenya.nomoblag.file.ConfigManager;
import com.zenya.nomoblag.file.MessagesManager;
import com.zenya.nomoblag.scheduler.DespawnEntityTask;
import com.zenya.nomoblag.scheduler.FreezeEntityTask;
import com.zenya.nomoblag.scheduler.TrackTPSTask;
import com.zenya.nomoblag.util.ChatUtils;
import com.zenya.nomoblag.util.LocationUtils;
import com.zenya.nomoblag.util.MetaUtils;
import com.zenya.nomoblag.util.SpawnerUtils;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class Listeners implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
        handleSpawnChance(e);
        handleSpawnTreshold(e);
        handleSpawners(e);
        handleMobCollisions(e);
        handleMobFreezing(e);
    }

    //Enforce anti-enderman end farm
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityTargetLivingEntityEvent(EntityTargetLivingEntityEvent e) {
        if(!MetaUtils.hasMeta(e.getEntity(), "no-endermite-aggro")) return;
        if(e.getTarget().getType().equals(EntityType.valueOf("ENDERMITE"))) {
            e.setTarget(null);
            e.setCancelled(true);
        }
    }

    //Enforce minimum spawner distance
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        if(e.isCancelled()) return;

        if(e.getBlockPlaced().getType().equals(XMaterial.SPAWNER.parseMaterial())) {
            Location blockLoc = e.getBlock().getLocation();

            CreatureSpawner nearestSpawner = SpawnerUtils.getNearestSpawner(blockLoc, ConfigManager.getInstance().getInt("spawners.minimum-spawner-distance"));
            if(nearestSpawner != null) {
                ChatUtils.sendMessage(e.getPlayer(), MessagesManager.getInstance().getString("spawners-too-close"));
                XParticle.line(LocationUtils.getCentre(blockLoc), LocationUtils.getCentre(nearestSpawner.getLocation()), 0.1*blockLoc.distance(nearestSpawner.getLocation()), ParticleDisplay.colored(blockLoc, java.awt.Color.RED, 1));
                e.setCancelled(true);
            }
        }
    }

    //Enforce mob unfreezing on interact
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent e) {
        if(!ConfigManager.getInstance().getBool("mob-freezing.enable-freezing")) return;
        if(!ConfigManager.getInstance().getBool("mob-freezing.enable-ai-on-interact")) return;
        if(e.isCancelled()) return;
        if(!(e.getRightClicked() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) e.getRightClicked();
        ArrayList<String> freezeBypassMobs = ConfigManager.getInstance().getList("mob-freezing.freeze-bypass-mobs");
        if((freezeBypassMobs == null) || (freezeBypassMobs.size() == 0) || !(freezeBypassMobs.contains(entity.getType().name()))) {
            entity.setAI(true);
            new FreezeEntityTask(entity);
        }
    }

    //Enforce mob unfreezing on damage
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamageEvent(EntityDamageEvent e) {
        if(!ConfigManager.getInstance().getBool("mob-freezing.enable-freezing")) return;
        if(!ConfigManager.getInstance().getBool("mob-freezing.enable-ai-on-interact")) return;
        if(e.isCancelled()) return;
        if(!(e.getEntity() instanceof LivingEntity)) return;

        LivingEntity entity = (LivingEntity) e.getEntity();
        ArrayList<String> freezeBypassMobs = ConfigManager.getInstance().getList("mob-freezing.freeze-bypass-mobs");
        if((freezeBypassMobs == null) || (freezeBypassMobs.size() == 0) || !(freezeBypassMobs.contains(entity.getType().name()))) {
            entity.setAI(true);
            new FreezeEntityTask(entity);
        }
    }

    //Enforce mob (un)freezing on chunk load
    //Enforce (lack of) mob collision on chunk load
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChunkLoadEvent(ChunkLoadEvent e) {
        Entity[] entities = e.getChunk().getEntities();
        if(entities == null || entities.length == 0) return;

        //Enforce mob collidability
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    for(Entity ent : entities) {
                        if(ent instanceof LivingEntity) {
                            LivingEntity entity = (LivingEntity) ent;
                            ArrayList<String> collidableMobs = ConfigManager.getInstance().getList("mob-collisions.force-collision-mobs");
                            if((collidableMobs == null) || (collidableMobs.size() == 0) || !(collidableMobs.contains(entity.getType().name()))) {
                                entity.setCollidable(!ConfigManager.getInstance().getBool("mob-collisions.disable-mob-collision"));
                            }
                        }
                    }
                } catch(NoSuchElementException exc) {
                    //Silence errors
                }
            }
        }.runTaskAsynchronously(NoMobLag.getInstance());

        //Unfreeze all existing mobs if enable-freezing is disabled in config
        if(!ConfigManager.getInstance().getBool("mob-freezing.enable-freezing")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        for(Entity ent : entities) {
                            if(ent instanceof LivingEntity) {
                                LivingEntity entity = (LivingEntity) ent;
                                entity.setAI(true);
                            }
                        }
                    } catch(NoSuchElementException exc) {
                        //Silence errors
                    }
                }
            }.runTaskAsynchronously(NoMobLag.getInstance());
        } else {
            //Do not unfreeze mobs on chunk load if defined in config
            if(!ConfigManager.getInstance().getBool("mob-freezing.enable-ai-on-chunk-load")) return;

            //Run temporary freeze task
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        for(Entity ent : entities) {
                            if(ent instanceof LivingEntity) {
                                LivingEntity entity = (LivingEntity) ent;
                                ArrayList<String> freezeBypassMobs = ConfigManager.getInstance().getList("mob-freezing.freeze-bypass-mobs");
                                if((freezeBypassMobs == null) || (freezeBypassMobs.size() == 0) || !(freezeBypassMobs.contains(entity.getType().name()))) {
                                    entity.setAI(true);
                                    new FreezeEntityTask(entity);
                                }
                            }
                        }
                    } catch(NoSuchElementException exc) {
                        //Silence errors
                    }
                }
            }.runTaskAsynchronously(NoMobLag.getInstance());
        }
    }

    public void handleSpawnChance(CreatureSpawnEvent e) {
        Random randObj = new Random();
        int tps = Math.round(TrackTPSTask.getInstance().getAverageTps());

        if(e.isCancelled()) return;

        //Check if mob spawn reason allows for blocking of mob spawning
        boolean canBlock = false;
        if(!(ConfigManager.getInstance().getList("mob-spawning.spawnreason-tps-block") == null) && !(ConfigManager.getInstance().getList("mob-spawning.spawnreason-tps-block").size() == 0)) {
            for(String spawnReason : ConfigManager.getInstance().getList("mob-spawning.spawnreason-tps-block")) {
                if(e.getSpawnReason().name().equals(spawnReason.toUpperCase())) {
                    canBlock = true;
                }
            }
        }
        if(!canBlock) return;

        //Enforce TPS spawn chance
        int randNum = randObj.nextInt(100) + 1;
        int spawnChance = ConfigManager.getInstance().getInt("mob-spawning.spawn-chance-at-tps." + String.valueOf(tps));

        if(randNum > spawnChance) {
            e.setCancelled(true);
        }
    }

    public void handleSpawnTreshold(CreatureSpawnEvent e) {
        int tps = Math.round(TrackTPSTask.getInstance().getAverageTps());

        if(e.isCancelled()) return;

        //Check if tps is below the configured value to enable this feature
        if(tps > ConfigManager.getInstance().getInt("spawn-treshold.tps-treshold")) return;

        //Enforce anti-breeding
        if(ConfigManager.getInstance().getBool("spawn-treshold.disable-breeding")) {
            if(e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.BREEDING)) {
                e.setCancelled(true);
                return;
            }
        }

        //Enforce anti-raiding
        if(ConfigManager.getInstance().getBool("spawn-treshold.disable-raiding")) {
            if(e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.RAID)) {
                e.setCancelled(true);
                return;
            }
        }

        //Enforce anti-spawner farm
        if(ConfigManager.getInstance().getBool("spawn-treshold.farms.block-spawner-farm")) {
            if(e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) {
                e.setCancelled(true);
                return;
            }
        }
        //Enforce anti-portal farm
        if(ConfigManager.getInstance().getBool("spawn-treshold.farms.block-portal-farm")) {
            if(e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NETHER_PORTAL)) {
                if(e.getEntity().getType().equals(EntityType.valueOf("PIG_ZOMBIE")) || e.getEntity().getType().equals(EntityType.valueOf("ZOGLIN")) || e.getEntity().getType().equals(EntityType.valueOf("DROWNED"))) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
        //Enforce anti-pigman reinforcement farm
        if(ConfigManager.getInstance().getBool("spawn-treshold.farms.block-pigman-farm")) {
            if(e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.REINFORCEMENTS)) {
                if(e.getEntity().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                    if(e.getEntity().getType().equals(EntityType.valueOf("PIG_ZOMBIE")) || e.getEntity().getType().equals(EntityType.valueOf("ZOGLIN"))) {
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }

        //Enforce anti-enderman end farm
        if(ConfigManager.getInstance().getBool("spawn-treshold.farms.block-enderman-farm")) {
            if(e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
                if(e.getEntity().getWorld().getEnvironment().equals(World.Environment.THE_END)) {
                    if(e.getEntity().getType().equals(EntityType.valueOf("ENDERMAN"))) {
                        MetaUtils.setMeta(e.getEntity(), "no-endermite-aggro", true);
                        return;
                    }
                }
            }
        }
    }

    public void handleSpawners(CreatureSpawnEvent e) {
        if(e.isCancelled()) return;
        if(!e.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.SPAWNER)) return;

        //Set spawner data
        new BukkitRunnable() {
            @Override
            public void run() {
                CreatureSpawner spawner = SpawnerUtils.getNearestSpawner(e.getLocation(), 4);
                if(spawner == null) return;

                //Handle spawner activation range
                spawner.setSpawnRange(4);
                spawner.setRequiredPlayerRange(ConfigManager.getInstance().getInt("spawners.activation-range"));

                //Handle max mobs per minute
                spawner.setSpawnCount(1);
                spawner.setMinSpawnDelay(Integer.valueOf(60*20/ConfigManager.getInstance().getInt("spawners.max-mobs-per-minute")));
                spawner.setMaxSpawnDelay(Integer.valueOf(60*20/ConfigManager.getInstance().getInt("spawners.max-mobs-per-minute")));
            }
        }.runTask(NoMobLag.getInstance());

        //Handle mob despawning
        if(ConfigManager.getInstance().getInt("spawners.mob-despawn-rate") < 0) return;
        LivingEntity entity = e.getEntity();
        new DespawnEntityTask(entity);
    }

    public void handleMobCollisions(CreatureSpawnEvent e) {
        if(e.isCancelled()) return;
        if(!ConfigManager.getInstance().getBool("mob-collisions.disable-mob-collision")) return;

        LivingEntity entity = e.getEntity();
        ArrayList<String> collidableMobs = ConfigManager.getInstance().getList("mob-collisions.force-collision-mobs");
        if((collidableMobs == null) || (collidableMobs.size() == 0) || !(collidableMobs.contains(entity.getType().name()))) {
            entity.setCollidable(false);
        }
    }

    public void handleMobFreezing(CreatureSpawnEvent e) {
        if(e.isCancelled()) return;
        if(!ConfigManager.getInstance().getBool("mob-freezing.enable-freezing")) return;
        if(ConfigManager.getInstance().getInt("mob-freezing.disable-ai-after") < 0) return;

        LivingEntity entity = e.getEntity();
        ArrayList<String> freezeBypassMobs = ConfigManager.getInstance().getList("mob-freezing.freeze-bypass-mobs");
        if((freezeBypassMobs == null) || (freezeBypassMobs.size() == 0) || !(freezeBypassMobs.contains(entity.getType().name()))) {
            entity.setAI(true);
            new FreezeEntityTask(entity);
        }
    }
}