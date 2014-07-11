package pt.minecraft.itemdropper;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


class ItemDropperPoller extends BukkitRunnable
{
	
	
	private class ItemDropRunnable extends BukkitRunnable {

		@Override
		public void run()
		{
			while(!cancel)
			{
				try {
					synchronized(itemDropRunnable)
					{
						itemDropRunnable.wait();
					}
					
					Bukkit.getServer().getPluginManager().callEvent(new ItemDroppedEvent());
					
				} catch (InterruptedException e) { }
			}
			
		}
		
	}
	
	
	
	
	
    private final ItemDropperPlugin plugin;
    private volatile boolean cancel;
    private long interval;
    private HashMap<Integer, ItemDrop> undroppedItems = new HashMap<Integer, ItemDrop>();
    private ArrayList<ItemDrop> itemsToRemove = new ArrayList<ItemDrop>(); 
    private ItemDropRunnable itemDropRunnable = null;
    private BukkitTask itemDropTask = null;
    private final DB dbConn;

    
    public ItemDropperPoller(ItemDropperPlugin plugin) throws SQLException
    {
    	this.plugin = plugin;
        interval = plugin.getConfig().getInt("pollDatabase") * 1000L;
        
        dbConn = new DB(plugin);
        dbConn.init();
        
        itemDropRunnable = new ItemDropRunnable();
        itemDropTask = itemDropRunnable.runTask(plugin);
    }
    

    public void cancel()
    {
        cancel = true;
        
        itemDropTask.cancel();
        super.cancel();
    }
    
    
    private void sleepABit()
    {
        try
        {
            Thread.sleep(interval);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    public void run()
    {
    	PreparedStatement stmt = null;
        int id,accountid,itemdrop,itemnumber;
        short itemaux;
        String sql;
        ResultSet rs;
        ItemDrop drop;
        ArrayList<ItemDrop> dropQueue = new ArrayList<ItemDrop>();
        
        cancel = false;
        
        if( dbConn == null )
        	return;

        try {
        	
	        while( !cancel )
	        {
	            try
	            {
	                sql = String.format("SELECT * FROM `%s` WHERE  `active` = 1 AND `takendate` IS null", DB.TABLE_NAME);
	                stmt = dbConn.prepare(sql);
	
	                while( !cancel )
	                {
	            		if( itemsToRemove.size() > 0 )
	            		{
		                	synchronized( undroppedItems )
		                	{
			                	synchronized( itemsToRemove )
			                	{
				                	for(ItemDrop iDrop : itemsToRemove)
				                	{
				                		undroppedItems.remove(iDrop.getId());
				                	}

				                	itemsToRemove.clear();
			                	}
		                	}
	            		}
	                	
	                    rs = stmt.executeQuery();
	                    
	                    while( rs.next() )
	                    {
	                    	id = rs.getInt("id");
	                        
	                        if( !undroppedItems.containsKey(id) )
	                        {	
		                        accountid = rs.getInt("accountid");
		                        itemdrop = rs.getInt("itemdrop");
		                        itemaux = rs.getShort("itemaux");
		                        itemnumber = rs.getInt("itemnumber");
		                        
		                        drop = new ItemDrop(id, accountid, itemdrop, itemnumber, itemaux);
		                        
		                        dropQueue.add(drop);
	                        }
	                    }
	                    
	                    if( dropQueue.size() > 0 )
	                    {
	                    	if( plugin.isDebugMode() );
	                			Utils.info("Received %d new items from database", dropQueue.size());
	                		
	                    	synchronized(undroppedItems)
	                    	{
	                    		for(ItemDrop d : dropQueue)
	                    			undroppedItems.put(d.getId(), d);
	                    	}
	                    	
	                    	dropQueue.clear();
	                    	
	                    	synchronized(itemDropRunnable)
	                    	{
		                    	// send the event now
	                    		itemDropRunnable.notify();
	                    	}
	                    }
	                    
	                    if( !cancel )
	                    	sleepABit();
	                }
	                
	            } catch (SQLException e) {
	            	
	            	if( plugin.isDebugMode() )
	            		Utils.severe(e, "Error while executing query");
	            		
	            } finally {
	            	try {
						DB.close(stmt);
					} catch (SQLException e) { }
	            }
	            
	            if( !cancel )
	            	sleepABit();
	        }
        } finally {
        	dbConn.close();
        }
    }
    
    
    // WARNING: Always synchronize while modifying this map
    public HashMap<Integer, ItemDrop> getUndroppedMap()
    {
    	return this.undroppedItems;
    }
    
    public void removeItemDrop(ItemDrop item)
    {	
    	if( item.getRemoveDate() <= 0 )
    		item.setRemoveDate();
    	
    	synchronized(itemsToRemove)
    	{
    		itemsToRemove.add(item);
    	}
    }
}

