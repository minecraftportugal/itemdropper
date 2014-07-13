package pt.minecraft.itemdropper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.luricos.bukkit.xAuth.PlayerManager;
import de.luricos.bukkit.xAuth.xAuth;
import de.luricos.bukkit.xAuth.xAuthPlayer;
import de.luricos.bukkit.xAuth.xAuthPlayer.Status;
import de.luricos.bukkit.xAuth.events.xAuthLoginEvent;

public class XAuthLoginListener implements Listener {
	
	
	
	private PlayerManager manager = null;
	
	public XAuthLoginListener()
	{
		xAuth plugin = (xAuth) Bukkit.getServer().getPluginManager().getPlugin(PlayerProvider.AuthPluginType.XAUTH.getName());
		
		manager = plugin.getPlayerManager();
	}
	
	
	
	
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoinEvent(PlayerJoinEvent event)
	{
		Player p = event.getPlayer();
		xAuthPlayer xp = null;
		
		//Utils.info("PlayerJoinEvent: %s, %s", xp.getName(),  xp.getStatus().toString());
		
		if( p == null )
			return;
		
		xp = manager.getPlayer(p);
		
		if(    xp != null 
			&& xp.isOnline()
			&& xp.getStatus() == Status.AUTHENTICATED )
			emiteEvent();
	}
	
	
	
	@EventHandler(priority=EventPriority.NORMAL)
	public void onXauthLoginEvent(xAuthLoginEvent event)
	{
		if(    event != null
			&& event.getAction() == xAuthLoginEvent.Action.PLAYER_LOGIN
			&& event.getStatus() == Status.AUTHENTICATED )
			emiteEvent();
	}

	
	
	private void emiteEvent()
	{
		 // force a check, may this user have items to be delivered
		Bukkit.getServer().getPluginManager().callEvent( new ItemDroppedEvent() );
	}


}
