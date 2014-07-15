package pt.minecraft.itemdropper;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


class ItemDropperPoller extends BukkitRunnable
{
	
	
	private class ItemDropRunnable extends BukkitRunnable {

		@Override
		public void run()
		{
//			while(!cancel)
//			{
//				try {
//					synchronized(itemDropRunnable)
//					{
//						itemDropRunnable.wait();
//					}
					
					Bukkit.getServer().getPluginManager().callEvent(new ItemDroppedEvent());
//					
//				} catch (InterruptedException e) { }
//			}
			
		}
		
	}
	
	
	
	
	
    private final ItemDropperPlugin plugin;
    private volatile boolean cancel;
    private long interval;
    private HashMap<Integer, ItemDrop> undroppedItems = new HashMap<Integer, ItemDrop>();
    private ArrayList<ItemDrop> itemsToRemove = new ArrayList<ItemDrop>(); 
    //private ItemDropRunnable itemDropRunnable = null;
    //private BukkitTask itemDropTask = null;
    private final DB dbConn;

    
    public ItemDropperPoller(ItemDropperPlugin plugin) throws SQLException
    {
    	this.plugin = plugin;
        interval = plugin.getConfig().getInt("pollDatabase") * 1000L;
        
        dbConn = new DB(plugin);
        
        if( !dbConn.init(true) )
        	throw new SQLException("Could not connect to database");
        
        if( plugin.isDebugMode() )
        	Utils.info("[DEBUG] Database poll interval: %d", interval);
        
//        itemDropRunnable = new ItemDropRunnable();
//        itemDropTask = itemDropRunnable.runTaskLater(plugin, 20);
    }
    

    public void cancel()
    {
        cancel = true;
        
        //itemDropTask.cancel();
        super.cancel();
    }
    
   

    public void run()
    {
    	PreparedStatement stmt = null;
        int id,accountid,itemdrop,itemnumber;
        int lastId = 0;
        short itemaux;
        ResultSet rs;
        ItemDrop drop;
        ArrayList<ItemDrop> dropQueue = new ArrayList<ItemDrop>();
        
        String sql = String.format("SELECT * FROM `%s` WHERE `id` > ? AND `takendate` IS null AND `active` = 1 ORDER BY `id` ASC;", dbConn.getTableName());
        
        cancel = false;
        
        if( dbConn == null )
        	return;
        
        try {

            while( !cancel )
            {
            	
            	// Remove items already delivered
            	
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
        		
	            try
	            {
	                stmt = dbConn.prepare(sql);
	                stmt.setInt(1, lastId);
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
                        
                        if( id > lastId )
                        	lastId = id;
                    }
                    
                    if( dropQueue.size() > 0 )
                    {
                    	if( plugin.isDebugMode() );
                			Utils.info("[DEBUG] Received %d new items from database", dropQueue.size());
                		
                    	synchronized(undroppedItems)
                    	{
                    		for(ItemDrop d : dropQueue)
                    			undroppedItems.put(d.getId(), d);
                    	}
                    	
                    	dropQueue.clear();
                    	
                    	(new ItemDropRunnable()).runTask(plugin);
                    	
//                    	synchronized(itemDropRunnable)
//                    	{
//	                    	// send the event now
//                    		itemDropRunnable.notify();
//                    	}
                    }
	                
	            } catch (SQLException e) {
	            	
	            	if( plugin.isDebugMode() )
	            		Utils.severe(e, "Error while executing query");
	            		
	            } finally {
	            	try {
						DB.close(stmt);
					} catch (SQLException e) { }
	            }
	            
	            try
	            {
	                Thread.sleep(interval);
	            }
	            catch (InterruptedException e)
	            {
	                cancel = true;
	            }
	        }
                
        } finally {
        	dbConn.close();
        }
    }
    
    
    // WARNING: Always synchronize while modifying/iterating this map
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
    
    public void removeItemDrops(List<ItemDrop> dropList)
    {	    	
    	synchronized(itemsToRemove)
    	{
    		for(ItemDrop item : dropList)
    		{
    	    	if( item.getRemoveDate() <= 0 )
    	    		item.setRemoveDate();
    	    	
        		itemsToRemove.add(item);
    		}
    	}
    }
    
}

