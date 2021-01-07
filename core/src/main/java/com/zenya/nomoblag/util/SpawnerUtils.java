package com.zenya.nomoblag.util;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;

import java.util.ArrayList;

public class SpawnerUtils {
    private static ArrayList<CreatureSpawner> getNearbySpawners(Location loc, double maxDist) {
        ArrayList<CreatureSpawner> spawners = new ArrayList<CreatureSpawner>();
        double minX = loc.getX() - maxDist;
        double maxX = loc.getX() + maxDist;
        double minY = loc.getY() - maxDist;
        double maxY = loc.getY() + maxDist;
        double minZ = loc.getZ() - maxDist;
        double maxZ = loc.getZ() + maxDist;
        for(double x=minX; x<=maxX; x++) {
            for(double y=minY; y<=maxY; y++) {
                for(double z=minZ; z<=maxZ; z++) {
                    Location checkLoc = new Location(loc.getWorld(), x, y, z);
                    if(!loc.equals(checkLoc) && loc.getWorld().getBlockAt(checkLoc).getType().equals(XMaterial.SPAWNER.parseMaterial())) {
                        spawners.add((CreatureSpawner) loc.getWorld().getBlockAt(checkLoc).getState());
                    }
                }
            }
        }
        return spawners;
    }

    public static CreatureSpawner getNearestSpawner(Location loc, double maxDist) {
        ArrayList<CreatureSpawner> spawners = getNearbySpawners(loc, maxDist);
        if(spawners == null || spawners.size() == 0) return null;

        CreatureSpawner closest = spawners.get(0);
        double minDist = loc.distance(closest.getLocation());
        for(CreatureSpawner spawner : spawners) {
            double checkDist = loc.distance(spawner.getLocation());
            if(checkDist < minDist) {
                minDist = checkDist;
                closest = spawner;
            }
        }
        return closest;
    }
}