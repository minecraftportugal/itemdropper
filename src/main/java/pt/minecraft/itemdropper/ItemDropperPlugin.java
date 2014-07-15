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
	private PlayerProvider playerProvider = null;
	private ItemDroppedListener listener = null;
	
	
	
	@Override
	public void onEnable() {

		PluginManager pluginManager = getServer().getPluginManager();
		
		debugMode = getConfig().getBoolean("debug");
		
		this.saveDefaultConfig();
		this.reloadConfig();
		
		try {
			
			playerProvider = new PlayerProvider(this);
			listener = new ItemDroppedListener(this);
			poller = new ItemDropperPoller(this);
			
			pluginManager.registerEvents(listener, this);
			
			XAuthLoginListener xAuthListener = XAuthLoginListener.safeInstance();
			// try to register the xAuth Listener\ compatible version	
			if( xAuthListener != null )
				pluginManager.registerEvents(xAuthListener, this);
			
			else
			if( isDebugMode() )
				Utils.severe("[DEBUG] Cannot register XAuthLoginListener, maybe incompatible version.");
			
	        poller.runTaskAsynchronously(this);
	        
	        this.saveConfig();
	        
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
		
		if( listener != null )
			listener.cancel();
		
		if( poller != null )
			poller.cancel();
		
		DB.configErrorBefore = false;
		
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



	public PlayerProvider getPlayerProvider()
	{
		return playerProvider;
	}
	
	
}
