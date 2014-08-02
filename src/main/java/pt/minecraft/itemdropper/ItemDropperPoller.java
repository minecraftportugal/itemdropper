package pt.minecraft.itemdropper;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


class ItemDropperPoller extends Thread
{
	
	
	private static class CallDropEvent implements Callable<Void> {

		@Override
		public Void call() throws Exception
		{
			
			Bukkit.getServer().getPluginManager().callEvent(new ItemDropCheckEvent());
			
			return null;
		}
		
	}
	
	
	
	
    private final ItemDropperPlugin plugin;
    private volatile boolean cancel;
    private long interval;
    private HashMap<Integer, ItemDrop> undroppedItems = new HashMap<Integer, ItemDrop>();
    private ArrayList<ItemDrop> itemsToRemove = new ArrayList<ItemDrop>(); 
    private final DB dbConn;
    private final CallDropEvent eventCallerInstance = new CallDropEvent();
    private final Object cancelMutex = new Object();

    
    public ItemDropperPoller(ItemDropperPlugin plugin) throws SQLException
    {
    	setDaemon(false);
    	
    	this.plugin = plugin;
    	int _interval = plugin.getConfig().getInt("pollDatabase", 1);
        interval = _interval * 1000L;
        
        dbConn = new DB(plugin);
        
        if( !dbConn.init(true) )
        	throw new SQLException("Could not connect to database");
        
        if( plugin.isDebugMode() )
        	Utils.debug("Database poll interval: %d", _interval);
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
        Future<Void> activeTaskFuture = null;
        
        String sql = String.format("SELECT * FROM `%s` WHERE `id` > ? AND `takendate` IS null AND `active` = 1 ORDER BY `id` ASC;", dbConn.getTableName());
        
        cancel = false;
        
        if( dbConn == null )
        	return;
        
        try {

            while( !cancel )
            {
            	
            	if( activeTaskFuture != null )
            	{
	            	try {
	            		activeTaskFuture.get();
					} catch (   InterruptedException 
							  | ExecutionException
							  | CancellationException e ) { }
	            	
	            	activeTaskFuture = null;
            	}
            	
            	
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
        		
        		stmt = null;
        		
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
                        
                    }
                    
                    if( dropQueue.size() > 0 )
                    {
                    	if( plugin.isDebugMode() );
                			Utils.debug("Received %d new items from database", dropQueue.size());
                		
                    	synchronized(undroppedItems)
                    	{
                    		for(ItemDrop d : dropQueue)
                    		{
                    			undroppedItems.put(d.getId(), d);
                    			
                                if( d.getId() > lastId )
                                	lastId = d.getId();
                    		}
                    	}
                    	
                    	dropQueue.clear();
                    	
                    	activeTaskFuture = Bukkit.getScheduler().callSyncMethod(this.plugin, eventCallerInstance);
                    }
	                
	            } catch (SQLException e) {
	            	
	            	if( plugin.isDebugMode() )
	            		Utils.debug(e, "Error while executing query");
	            		
	            } finally {
	            	DB.safeClose(stmt);
	            }
	            
	            if( cancel ) break;
	            
	            try
	            {
	            	synchronized(cancelMutex)
	            	{
	            		cancelMutex.wait(interval);
	            	}
	            	
	            } catch (InterruptedException e) {}
	            
	        }
                
        } finally {
        	dbConn.close();
        }
        
        if(this.plugin.isDebugMode())
        	Utils.debug("ItemDropperPoller ended running");
    }
    
    
    
    public void cancel()
    {
        cancel = true;
        
    	synchronized(cancelMutex)
    	{
    		cancelMutex.notify();
    	}
    }
    
    public void cancelJoin()
    {
    	this.cancel();
    	
    	try {
			this.join();
		} catch (InterruptedException e) { }
    	
        if(this.plugin.isDebugMode())
        	Utils.debug("ItemDropperPoller #cancelJoin");
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

