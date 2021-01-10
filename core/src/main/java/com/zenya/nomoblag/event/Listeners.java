package com.zenya.nomoblag.event;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.zenya.nomoblag.NoMobLag;
import com.zenya.nomoblag.file.ConfigManager;
import com.zenya.nomoblag.file.MessagesManager;
import com.zenya.nomoblag.scheduler.DespawnEntityTask;
import com.zenya.nomoblag.scheduler.FreezeEntityTask;
import com.zenya.nomoblag.scheduler.SetCollidableTask;
import com.zenya.nomoblag.scheduler.TrackTPSTask;
import com.zenya.nomoblag.util.ChatUtils;
import com.zenya.nomoblag.util.LocationUtils;
import com.zenya.nomoblag.util.MetaUtils;
import com.zenya.nomoblag.util.SpawnerUtils;
import org.bukkit.*;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
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
            new FreezeEntityTask(entity);
        }
    }

    //Fix projectiles not hitting entities when collision is disabled
    @EventHandler
    public void onProjectileLaunchEvent(ProjectileLaunchEvent e) {
        Projectile proj = e.getEntity();

        new BukkitRunnable() {
            @Override
            public void run() {
                if(proj == null || proj.isDead()) this.cancel();

                for(Entity ent : proj.getNearbyEntities(2, 2, 2)) {
                    if(ent instanceof LivingEntity) {
                        try {
                            new SetCollidableTask((LivingEntity) ent);
                        } catch(NoSuchMethodError exc) {
                            //Silence 1.8 errors
                        }
                    }
                }
            }
        }.runTaskTimer(NoMobLag.getInstance(), 0, 1);
    }

    //Call PlayerChunkChangeEvent
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        final int oldChunkX = player.getLocation().getChunk().getX();
        final int oldChunkZ = player.getLocation().getChunk().getZ();

        new BukkitRunnable() {
            @Override
            public void run() {
                Chunk currentChunk = player.getLocation().getChunk();
                if(currentChunk.getX() == oldChunkX && currentChunk.getZ() == oldChunkZ) return;
                Bukkit.getPluginManager().callEvent(new PlayerChunkChangeEvent(player, currentChunk));
            }
        }.runTaskLater(NoMobLag.getInstance(), 1);
    }

    //Enforce mob (un)freezing on chunk load
    //Enforce (lack of) mob collision on chunk load
    @EventHandler
    public void onPlayerChunkChangeEvent(PlayerChunkChangeEvent e) {
        ArrayList<LivingEntity> mobs = new ArrayList<>();
        for(Chunk chunk : e.getNearbyChunks(1)) {
            for(Entity ent: chunk.getEntities()) {
                if(ent instanceof LivingEntity) {
                    mobs.add((LivingEntity) ent);
                }
            }
        }
        if(mobs == null || mobs.size() == 0) return;

        //Enforce mob collidability
        new BukkitRunnable() {
            @Override
            public void run() {
                for(LivingEntity mob : mobs) {
                    ArrayList<String> collidableMobs = ConfigManager.getInstance().getList("mob-collisions.force-collision-mobs");
                    if((collidableMobs == null) || (collidableMobs.size() == 0) || !(collidableMobs.contains(mob.getType().name()))) {
                        try {
                            mob.setCollidable(!ConfigManager.getInstance().getBool("mob-collisions.disable-mob-collision"));
                        } catch(NoSuchMethodError exc) {
                            //Silence 1.8 errors
                        }
                    }
                }
            }
        }.runTaskAsynchronously(NoMobLag.getInstance());

        //Unfreeze all existing mobs if enable-freezing is disabled in config
        if(!ConfigManager.getInstance().getBool("mob-freezing.enable-freezing")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for(LivingEntity mob : mobs) {
                        mob.setAI(true);
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
                    for(LivingEntity mob : mobs) {
                        ArrayList<String> freezeBypassMobs = ConfigManager.getInstance().getList("mob-freezing.freeze-bypass-mobs");
                        if((freezeBypassMobs == null) || (freezeBypassMobs.size() == 0) || !(freezeBypassMobs.contains(mob.getType().name()))) {
                            new FreezeEntityTask(mob);
                        }
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
            try {
                entity.setCollidable(false);
            } catch(NoSuchMethodError exc) {
                //Silence 1.8 errors
            }
        }
    }

    public void handleMobFreezing(CreatureSpawnEvent e) {
        if(e.isCancelled()) return;
        if(!ConfigManager.getInstance().getBool("mob-freezing.enable-freezing")) return;
        if(ConfigManager.getInstance().getInt("mob-freezing.disable-ai-after") < 0) return;

        LivingEntity entity = e.getEntity();
        ArrayList<String> freezeBypassMobs = ConfigManager.getInstance().getList("mob-freezing.freeze-bypass-mobs");
        if((freezeBypassMobs == null) || (freezeBypassMobs.size() == 0) || !(freezeBypassMobs.contains(entity.getType().name()))) {
            new FreezeEntityTask(entity);
        }
    }
}