package pt.minecraft.itemdropper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ItemDroppedListener implements Listener {
	
	
	@SuppressWarnings("unused")
	private ItemDropperPlugin plugin;
	
	public ItemDroppedListener(ItemDropperPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onItemDropped(ItemDroppedEvent event)
	{
		
		
	}

}
