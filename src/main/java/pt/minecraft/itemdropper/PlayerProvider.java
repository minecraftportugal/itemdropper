package pt.minecraft.itemdropper;


import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;



public class PlayerProvider {
	
	
	public static enum AuthPluginType {
		NULL(null),
		XAUTH("xAuth");
		
		
		
		private final String name;
		
		AuthPluginType(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return this.name;
		}
		
	}
	
	private Plugin authPlugin = null;
	private AuthPluginType authPluginType = AuthPluginType.NULL;
	
	private ItemDropperPlugin dropperPlugin;
	
	public PlayerProvider(ItemDropperPlugin plugin)
	{
		this.dropperPlugin = plugin;
	}
	
	
	private Plugin getAuthPluginIfEnabled(AuthPluginType type)
	{
		Plugin plugin = null;
		
		if( type == AuthPluginType.NULL )
			return null;
		
		plugin = dropperPlugin.getServer().getPluginManager().getPlugin(type.getName());
		
		if(    plugin != null
			&& plugin.isEnabled() )
		{
			try {
				
				switch(type)
				{
					case XAUTH:

						if( plugin.getClass().isAssignableFrom( de.luricos.bukkit.xAuth.xAuth.class ))
							return plugin;
						
					default:
						return null;
				}
				
			} catch(Exception e) { }
		}
		
		
		return null;
	}
	
	public Plugin getAuthPlugin()
	{
		if(    authPlugin == null
			|| !authPlugin.isEnabled() )
		{
			Plugin plugin = null;
			
			authPlugin = null;
			
			// try xAuth
			plugin = getAuthPluginIfEnabled(AuthPluginType.XAUTH);
			
			if( plugin != null )
			{
				authPlugin = plugin;
				authPluginType = AuthPluginType.XAUTH;
			}
			else
			{
				//TODO: Authme
				
				
			}
				
		}
		
		return authPlugin;
	}

	
	
	public Player getAuthenticatedPlayer(int accountID)
	{
		getAuthPlugin();
		
		if( authPlugin != null )
		{	
			try {
				
				switch(authPluginType)
				{
					case XAUTH:
						
						xAuthPlayer xPlayer = null;
						xAuth xAuthPlugin = (xAuth)authPlugin;

						xPlayer = xAuthPlugin.getPlayerManager().getPlayerById( accountID );
						
						if(    xPlayer != null
							&& xPlayer.isOnline()
							&& xPlayer.getStatus() == xAuthPlayer.Status.AUTHENTICATED)
							return xPlayer.getPlayer();
						
						break;
						
					default:
						break;
				}
				
			}catch(Exception e) {
				if(dropperPlugin.isDebugMode())
					Utils.debug(e, "Error getting player from plugin: %s", authPluginType.getName());
			}
		}
		
		return null;
	}
}
