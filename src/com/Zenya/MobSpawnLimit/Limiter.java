package com.Zenya.MobSpawnLimit;

import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class Limiter implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void SpawnEvent(CreatureSpawnEvent e) {
		if ((Main.instance.getConfig().getBoolean("reduce-spawner-only") && e.getSpawnReason() == SpawnReason.SPAWNER) || (!Main.instance.getConfig().getBoolean("reduce-spawner-only"))) {
			int tps = (int) Main.tps;
			Random randObj = new Random();
			int randNum = randObj.nextInt(100) + 1;
			int chance = ConfigManager.getInstance().getChanceAt(tps);

			if(randNum > chance) {
				e.setCancelled(true);
			}
		}
	}
}
