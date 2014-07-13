package pt.minecraft.itemdropper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;


public class ItemDroppedListener implements Listener {
	
	
	
	private class DataBaseUpdaterRunnable extends BukkitRunnable
	{
		private ArrayList<ItemDrop> itemListQueue = new ArrayList<ItemDrop>();
		private boolean isRunning = false;
		
		
		public boolean isRunning()
		{
			return isRunning;
		}
		
		@Override
		public void run()
		{
			ArrayList<ItemDrop> toUpdate = new ArrayList<ItemDrop>();
			PreparedStatement stmt;
			String sql = String.format("UPDATE `%s` SET `takendate` = ? WHERE `id` = ?", DB.TABLE_NAME);
			
			isRunning = true;
			
			try {
			
				while(!cancel)
				{
					//TODO: maybe add some thread sleep here to make updates go in bursts of x seconds ?
	
					// wait for notify if there isn't already new updates on queue
					synchronized( dataBaseUpdater )
					{
						if( itemListQueue.size() == 0 )
						{
							try {
								dataBaseUpdater.wait(1000); // wake every second to check if task was cancelled
								
							} catch (InterruptedException e) {
								cancel = true;
								break;
							}
						}
					}
					
					// grab last items to update from queue
					synchronized( itemListQueue )
					{
						if( itemListQueue.size() == 0 )
							continue;
						
						toUpdate.addAll(itemListQueue);
						itemListQueue.clear();
					}
					
					// update those items
					if( toUpdate.size() > 0 )
					{
						for( ItemDrop drop: toUpdate )
						{
							if( drop.getRemoveDate() <= 0 )
								drop.setRemoveDate();
	  
				            try {
				            	
								stmt = dbConn.prepare(sql);
					            stmt.setTimestamp(1, new Timestamp(drop.getRemoveDate()));
					            stmt.setInt(2, drop.getId());
					            stmt.executeUpdate();
					            
							} catch (SQLException e) {
								if( plugin.isDebugMode() )
									Utils.severe(e, "[DEBUG] Error while updating delivered item: %d", drop.getId());
							}
						}
						
						plugin.getPoller().removeItemDrops(toUpdate);
						toUpdate.clear();
					}
				}
				
			} finally {
				if( dbConn != null )
					dbConn.close();
			}

		}
		
		@Override
		public synchronized void cancel() throws IllegalStateException
		{
			cancel = true;
			
			super.cancel();
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
			if( dbConn != null )
				dbConn.close();

			super.finalize();
		}
		
	}
	
	
	
	
	private ItemDropperPlugin plugin;
	private DB dbConn = null;
	private boolean cancel = false;
	private DataBaseUpdaterRunnable dataBaseUpdater = null;
	private String messageDelivered = null;
	private String messageDropped = null;
	
	
	public ItemDroppedListener(ItemDropperPlugin plugin) throws SQLException
	{
		this.plugin = plugin;
		this.dbConn = new DB(plugin);
		
		if( !dbConn.init(false) )
			throw new SQLException("Could not connect to database");
		
		messageDelivered = plugin.getConfig().getString("message.delivered");
		messageDelivered = plugin.getConfig().getString("message.dropped");
		
		dataBaseUpdater = new DataBaseUpdaterRunnable();
		dataBaseUpdater.runTaskAsynchronously(plugin);
		
		// wait until background thread is started
		while(dataBaseUpdater.isRunning())
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if( plugin.isDebugMode() )
			Utils.info("[DEBUG] ItemDroppedListener started correctly");
	}
	
	
	
	

	@SuppressWarnings("deprecation")
	private void deliverItemToPlayer(ItemDrop item)
	{
		ItemStack is;
		String iname;
		HashMap<Integer, ItemStack> leftOver;
		Player player = item.getPlayer();
		
		if( player == null )
			return;
		
		iname = Utils.nameTreat(Material.getMaterial(item.getItem()).name());
		
		if( plugin.isDebugMode() )
			Utils.info("[DEBUG] Delivering %d '%s' to '%s'", item.getSize(), iname, player.getName());
		
		is = new ItemStack(item.getItem(), item.getSize(), item.getItemAux());
        leftOver = new HashMap<Integer, ItemStack>( player.getInventory().addItem(is) );

        Utils.sendMessage(
        		String.format(
        				messageDelivered,
        				Integer.toString(item.getSize()),
        				iname ),
        		player);

        if( leftOver.size() > 0 )
        {
        	for(Entry<Integer, ItemStack> s : leftOver.entrySet())
	        {
        		player.getWorld().dropItemNaturally(
					        				player.getEyeLocation(),
					                		s.getValue() );
	        }
        	
            Utils.sendMessage( messageDropped, player);
        }
        
        // reset remove date as NOW
		item.setRemoveDate();
	}
	
	
	
	// this listener runs on main thread, lock as less as possible
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onItemDropped(ItemDroppedEvent event)
	{
		ItemDropperPoller poller = plugin.getPoller();
		HashMap<Integer, ItemDrop> undeliveredDrops = null;
		ArrayList<ItemDrop> toDrop = new ArrayList<ItemDrop>();
		PlayerProvider provider = plugin.getPlayerProvider();
		Player player = null;
		
		if( poller == null )
			return;
		
		if( plugin.isDebugMode() )
			Utils.info("[DEBUG] Received ItemDroppedEvent");
		
		undeliveredDrops = poller.getUndroppedMap();
		
		// don't let background thread change this map while we are iterating
		synchronized(undeliveredDrops)
		{
			for(ItemDrop drop : undeliveredDrops.values())
			{
				if( drop.getRemoveDate() > 0 )
					continue;
				
				player = provider.getAuthenticatedPlayer(drop.getAccountId());
				
				if( player != null )
				{
					// set remove date while synchronizing, so the items is not delivered twice in case a new event is fired
					drop.setRemoveDate();
					
					drop.setPlayer(player);
					toDrop.add( drop );
				}
			}
		}
		
		
		if( toDrop.size() > 0 )
		{
			for(ItemDrop drop : toDrop)
			{
				try {
					deliverItemToPlayer(drop);
				} catch( Exception e) { }
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
