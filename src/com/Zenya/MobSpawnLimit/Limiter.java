package com.Zenya.MobSpawnLimit;

import java.util.Random;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class Limiter implements Listener {
	int c;
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void SpawnEvent(CreatureSpawnEvent e) {
		if (e.getSpawnReason() == SpawnReason.SPAWNER) {
			int tps = (int) Main.tps;
			Random r = new Random();
			
			switch(tps) {
			case 20: // 1.0 chance of spawning
			case 19:
				c = -1; // -1 -> 0% cancel
				break;
				
			case 18: // 0.5 chance of spawning
			case 17:
				c = r.nextInt(2); // 0 - 1
				break;
				
			case 16: // 0.25 chance of spawning
			case 15:
				c = r.nextInt(4); // 0 - 3
				break;
				
			case 14: // 0.1 chance of spawning
			case 13:
				c = r.nextInt(10); // 0 - 9
				break;
				
			case 12: //Cancel
			case 11:
			default:
				c = 1; // 1 -> 100% cancel
				break;
			}
			
			if(c > 0) {
				e.setCancelled(true);
			}
			return;
		}
	}
}
/*			int c = r.nextInt(tps-(tps-(n/10))) + (tps-(n/10));
			
			if (c <= 0 || c*c < n) {
				e.setCancelled(true);
			}
		}
	}
}

when TPS is 20:
- min chance is 10^2 > 100
- mobs will spawn regardless

when TPS is 15:
- min chance is 5^2 = 25
- max chance is 15^2 = 225
- av. chance is 10^2 = 100
- mobs will spawn around 50% of the time?

when TPS is 10:
- min chance is 0^2 = 0
- max chance is 10^2 = 100
- mobs will never spawn when TPS nears 10, which means its pretty laggy
 */
