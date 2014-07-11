package pt.minecraft.itemdropper;

import java.sql.SQLException;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemDropperPlugin extends JavaPlugin {
	
	public static final String PLUGIN_NAME = "ItemDropper";
	
	private static final String INIT_FAIL_MESSAGE = "failed to enable plugin";
	
	private boolean debugMode = false;
	private ItemDropperPoller poller = null;
	
	
	
	@Override
	public void onEnable() {

		PluginManager pluginManager = getServer().getPluginManager();
		
		debugMode = getConfig().getBoolean("debug");
		
		try {
			
			pluginManager.registerEvents(new ItemDroppedListener(this), this);
			
			poller = new ItemDropperPoller(this);
	        poller.runTaskAsynchronously(this);
	        
			Utils.info("enabled successfully");	

		} catch (SQLException e) {


			
			if( debugMode )
				Utils.severe(e, INIT_FAIL_MESSAGE);
			else
				Utils.severe(INIT_FAIL_MESSAGE);
			
			pluginManager.disablePlugin(this);
	        

		}
	}
	
	

	@Override
	public void onDisable()
	{
		HandlerList.unregisterAll(this);
		
		if( poller != null )
			poller.cancel();
		
		Utils.info("disabled successfully");
	}
	
	
	public ItemDropperPoller getPoller()
	{
		return this.poller;
	}
	

	
	
	public boolean isDebugMode()
	{
		return this.debugMode;
	}
	
	
}
