package pt.minecraft.itemdropper;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class TimedCheckRunnable extends BukkitRunnable {

	@Override
	public void run() {

		Bukkit.getServer().getPluginManager().callEvent( new ItemDroppedEvent() );
		
	}
	
	
	

}
