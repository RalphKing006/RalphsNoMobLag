package com.Zenya.MobSpawnLimit;

//import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	public static Main instance;

	public static float tps = 20.0f;
	//public float atps = 0.0f;
	
	@Override
	public void onEnable() {
		instance = this;
		new ConfigManager(this);

		Bukkit.getServer().getPluginManager().registerEvents(new Limiter(/*this*/), this);
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			
			long start = 0;
			long now = 0;
			//ArrayList<Float> ttps = new ArrayList<>();

			@Override
			public void run() {
				//Main main = new Main();
				
				start = now;
				now = System.currentTimeMillis();
				long tdiff = now - start;
				
				if(tdiff != 0) {
					tps = (float) (1000 / tdiff);
				}
				
				if(tps > 20.0f) {
					tps = 20.0f;
				}
/*				
				ttps.add(tps);
				
				if (ttps.size() > 20) {
					ttps.remove(0);
				}
				
				int i = 0;
				atps = 0;
				while(i < ttps.size()) {
					atps += (ttps.get(i) / ttps.size());
					i++;
				}
				
				System.out.println("TPS: " + tps + ", ATPS: " + atps); */
				
			}
		}, 0, 1);
	}
	
	@Override
	public void onDisable() {
		Bukkit.getScheduler().cancelTasks(this);
	}
}

//tps = passed ticks / (end time - start time) * 1000