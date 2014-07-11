package pt.minecraft.itemdropper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ItemDroppedListener implements Listener {
	
	
	
	private class DataBaseUpdaterRunnable extends BukkitRunnable
	{
		private ArrayList<ItemDrop> itemListQueue = new ArrayList<ItemDrop>();
		
		@Override
		public void run()
		{
			ArrayList<ItemDrop> toUpdate = new ArrayList<ItemDrop>();
			
			while(!cancel)
			{
				//TODO: add some thread sleep to make updates go in burts of x seconds ?

				// wait for notify if there isn't alreay new updates on queue
				synchronized( dataBaseUpdater )
				{
					if( itemListQueue.size() == 0 )
					{
						try {
							dataBaseUpdater.wait(1000); // wake up every second to check if cancelled
							
						} catch (InterruptedException e) {
							cancel = true;
							break;
						}
					}
				}
				
				if( cancel )
					break;
				
				// grab last items to update from queue
				synchronized( itemListQueue )
				{
					if( itemListQueue.size() > 0 )
					{
						toUpdate.addAll(itemListQueue);
						itemListQueue.clear();
					}
				}
				
				// update those items
				if( toUpdate.size() > 0 )
				{
					for( ItemDrop drop: toUpdate)
					{
					
						//TODO: update database
					}
					
					toUpdate.clear();
				}

	
			}

		}
		
		
		public void addToUpdateQueue(ArrayList<ItemDrop> dropList)
		{
			if(    dropList == null
				|| dropList.size() == 0 )
				return;
			
			synchronized( itemListQueue )
			{
				itemListQueue.addAll(dropList);
			}
		}
		
		@Override
		protected void finalize() throws Throwable
		{
			dbConn.close();

			super.finalize();
		}
		
	}
	
	
	
	
	private ItemDropperPlugin plugin;
	private DB dbConn = null;
	private boolean cancel = false;
	private DataBaseUpdaterRunnable dataBaseUpdater = null;
	
	
	public ItemDroppedListener(ItemDropperPlugin plugin) throws SQLException
	{
		this.plugin = plugin;
		this.dbConn = new DB(plugin);
		
		dataBaseUpdater = new DataBaseUpdaterRunnable();
		
		dbConn.init(false);
	}
	
	// this listener runs on main thread, lock as less as possible
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onItemDropped(ItemDroppedEvent event)
	{
		ItemDropperPoller poller = plugin.getPoller();
		HashMap<Integer, ItemDrop> undeliveredDrops = null;
		ArrayList<ItemDrop> toDrop = new ArrayList<ItemDrop>();
		
		if( poller == null )
			return;
		
		undeliveredDrops = poller.getUndroppedMap();
		
		// don't let background thread change this map while we are iterating
		synchronized(undeliveredDrops)
		{
			for(ItemDrop drop : undeliveredDrops.values())
			{
				//TODO: check if player is online and logged in
				
				// if online && logged in
				toDrop.add(drop);
				// endif
			}
		}
		
		
		if( toDrop.size() > 0 )
		{
			for(ItemDrop drop : toDrop)
			{
				//TODO: do the actual delivery
				
				// Set remove date as NOW
				drop.setRemoveDate();
			}
			
			dataBaseUpdater.addToUpdateQueue( toDrop );
			
			synchronized( dataBaseUpdater )
			{
				dataBaseUpdater.notify();
			}
		}
	}
	
	
	public void cancel()
	{
		this.cancel = true;

		// wake up updater thread if its sleeping
		synchronized( dataBaseUpdater )
		{
			dataBaseUpdater.notify();
		}
	}


}
