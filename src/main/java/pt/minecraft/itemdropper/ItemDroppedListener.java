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


public class ItemDroppedListener implements Listener {
	
	
	
	private class DataBaseUpdatedThread extends Thread
	{
		private ArrayList<ItemDrop> itemListQueue = new ArrayList<ItemDrop>();
		private boolean cancel = false;
	
		
		public DataBaseUpdatedThread()
		{
			setDaemon(false);
		}
		
		
		@Override
		public void run()
		{
			ArrayList<ItemDrop> toUpdate = new ArrayList<ItemDrop>();
			PreparedStatement stmt = null;
			String sql = String.format("UPDATE `%s` SET `takendate` = ? WHERE `id` = ?", dbConn.getTableName());
			
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
					
					if( cancel ) break;
					
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
							
							stmt = null;
	  
				            try {
								stmt = dbConn.prepare(sql);
					            stmt.setTimestamp(1, new Timestamp(drop.getRemoveDate()));
					            stmt.setInt(2, drop.getId());
					            stmt.executeUpdate();
					            
							} catch (SQLException e) {
								if( plugin.isDebugMode() )
									Utils.debug(e, "Error while updating delivered item: %d", drop.getId());
							} finally {
								DB.safeClose(stmt);
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

	        if(ItemDroppedListener.this.plugin.isDebugMode())
	        	Utils.debug("DataBaseUpdatedThread ended running");
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
	private DataBaseUpdatedThread dataBaseUpdater = null;
	private String messageDelivered = null;
	private String messageDropped = null;
	
	
	public ItemDroppedListener(ItemDropperPlugin plugin) throws SQLException
	{
		this.plugin = plugin;
		this.dbConn = new DB(plugin);
		
		if( !dbConn.init(false) )
			throw new SQLException("Could not connect to database");
		
		messageDelivered = plugin.getConfig().getString("messages.delivered");
		messageDropped = plugin.getConfig().getString("messages.dropped");
		
		dataBaseUpdater = new DataBaseUpdatedThread();
		dataBaseUpdater.start();
		
		// wait until background thread is started
		while(!dataBaseUpdater.isAlive())
		{
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if( plugin.isDebugMode() )
			Utils.debug("ItemDroppedListener started correctly");
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
			Utils.debug("Delivering %d '%s' to '%s'", item.getSize(), iname, player.getName());
		
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
	public void onItemDropCheck(ItemDropCheckEvent event)
	{
		ItemDropperPoller poller = plugin.getPoller();
		HashMap<Integer, ItemDrop> undeliveredDrops = null;
		ArrayList<ItemDrop> toDrop = new ArrayList<ItemDrop>();
		PlayerProvider provider = plugin.getPlayerProvider();
		Player player = null;
		
		if( poller == null )
			return;
		
		if( plugin.isDebugMode() )
			Utils.debug("Received ItemDropCheckEvent");
		
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
	
	
	public boolean cancel()
	{
		if( dataBaseUpdater == null )
			return false;
		
		dataBaseUpdater.cancel = true;

		// wake up updater thread if its sleeping
		synchronized( dataBaseUpdater )
		{
			dataBaseUpdater.notify();
		}
		
		return true;
	}
	
    public void cancelJoin()
    {
    	if( !this.cancel() )
    		return;
    	
    	try {
    		dataBaseUpdater.join();
		} catch (InterruptedException e) { }
    	
        if(this.plugin.isDebugMode())
        	Utils.debug("DataBaseUpdatedThread #cancelJoin");
    }


}
