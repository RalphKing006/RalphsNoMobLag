package com.zenya.nomoblag.util;

import org.bukkit.Location;

public class LocationUtils {
    public static Location getCentre(Location loc) {
        return new Location(loc.getWorld(), loc.getX()+0.5, loc.getY()+0.5, loc.getZ()+0.5);
    }
}
