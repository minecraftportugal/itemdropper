package pt.minecraft.itemdropper;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import de.luricos.bukkit.xAuth.events.xAuthLoginEvent;
import de.luricos.bukkit.xAuth.events.xAuthPlayerJoinEvent;

public class XAuthLoginListener implements Listener {
	
	
	
	//private PlayerManager manager = null;

	private XAuthLoginListener()  { }

	
	
//	@EventHandler(priority=EventPriority.MONITOR)
//	public void onPlayerJoinEvent(PlayerJoinEvent event)
//	{
//		Player p = event.getPlayer();
//		xAuthPlayer xp = null;
//		
//		//Utils.info("PlayerJoinEvent: %s, %s", xp.getName(),  xp.getStatus().toString());
//		
//		if( p == null )
//			return;
//		
//		xp = manager.getPlayer(p);
//		
//		if(    xp != null 
//			&& xp.isOnline()
//			&& xp.getStatus() == Status.AUTHENTICATED )
//			emiteEvent();
//	}
	
	
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
			Bukkit.getServer().getPluginManager().callEvent( new ItemDroppedEvent() );
		}
	}
	
	public static XAuthLoginListener safeInstance()
	{
		Plugin jPlugin = Bukkit.getServer().getPluginManager().getPlugin(PlayerProvider.AuthPluginType.XAUTH.getName());
		
		if( jPlugin == null )
			return null;
		
		xAuth plugin = (xAuth)jPlugin;
		Method m = null;
		
		try {
			m = xAuth.class.getMethod("hasVersionFix");
		} catch(Exception e) { }
		
		if( m == null )
			return null;
		
		plugin = (xAuth) Bukkit.getServer().getPluginManager().getPlugin(PlayerProvider.AuthPluginType.XAUTH.getName());

		return ( plugin.hasVersionFix() < 1 ) ? null : new XAuthLoginListener();

	}


}
