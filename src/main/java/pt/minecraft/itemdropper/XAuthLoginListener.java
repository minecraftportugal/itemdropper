package pt.minecraft.itemdropper;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import de.luricos.bukkit.xAuth.xAuthPlayer;
import de.luricos.bukkit.xAuth.events.xAuthLoginEvent;
import de.luricos.bukkit.xAuth.events.xAuthPlayerJoinEvent;

public class XAuthLoginListener implements Listener {
	
	
	
	//private PlayerManager manager = null;

	private XAuthLoginListener()  { }

	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onXauthLoginEvent(xAuthPlayerJoinEvent event)
	{
		if(    event != null
			&& event.getAction() == xAuthPlayerJoinEvent.Action.PLAYER_JOINED )
			maybeEmitEvent(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onXauthLoginEvent(xAuthLoginEvent event)
	{
		if(    event != null
			&& event.getAction() == xAuthLoginEvent.Action.PLAYER_LOGIN )
			maybeEmitEvent(event.getPlayer());
	}

	
	
	private void maybeEmitEvent(xAuthPlayer player)
	{
		if(    player != null
			&& player.isOnline()
			&& player.getStatus() == xAuthPlayer.Status.AUTHENTICATED )
		{
			// force a check, may this user have items to be delivered
			Bukkit.getServer().getPluginManager().callEvent( new ItemDropCheckEvent() );
		}
	}
	
	public static XAuthLoginListener safeInstance(ItemDropperPlugin plugin)
	{
		Plugin jPlugin = Bukkit.getServer().getPluginManager().getPlugin(PlayerProvider.AuthPluginType.XAUTH.getName());
		Method xLoginMethod = null;
		Method xJoinMethod = null;
		
		if( jPlugin != null )
		{
			try {
				xLoginMethod = xAuthLoginEvent.class.getMethod("getPlayer");
				if( xLoginMethod.getReturnType() != xAuthPlayer.class )
					xLoginMethod = null;
				
				xJoinMethod = xAuthPlayerJoinEvent.class.getMethod("getPlayer");
				if( xJoinMethod.getReturnType() != xAuthPlayer.class )
					xJoinMethod = null;
			} catch(Exception e) {
				if( plugin.isDebugMode() )
					Utils.severe(e, "Not using proper xAuth");
			}
		}
		
		return (    xLoginMethod == null
				 || xJoinMethod  == null )
						? null
						: new XAuthLoginListener();
	}


}
