package pt.minecraft.itemdropper;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


public class Utils {

	private static Logger logger = Logger.getLogger("Minecraft");
	
	
    public static void info(String msg, Object ... args)
    {
        if (args.length > 0)
            msg = String.format(msg, args);
        
        msg = ChatColor.stripColor(msg);
        
        if (msg.isEmpty())
        	return;
        
        logger.log(Level.INFO, String.format("[%s] %s", ItemDropperPlugin.PLUGIN_NAME, msg));
    }

    public static void warning(String msg, Object ... args)
    {
        if (args.length > 0)
            msg = String.format(msg, args);
        
        msg = ChatColor.stripColor(msg);
        
        if (msg.isEmpty())
        	return;
        
        logger.log(Level.WARNING, String.format("[%s] %s", ItemDropperPlugin.PLUGIN_NAME, msg));
    }

    public static void severe(String msg, Object ... args)
    {
        if (args.length > 0)
            msg = String.format(msg, args);
        
        msg = ChatColor.stripColor(msg);
        
        if (msg.isEmpty())
        	return;
        
        logger.log(Level.SEVERE, String.format("[%s] %s", ItemDropperPlugin.PLUGIN_NAME, msg));
    }

    public static void severe(Throwable t, String msg, Object ... args)
    {
        if (args.length > 0)
            msg = String.format(msg, args);
        
        msg = ChatColor.stripColor(msg);
        
        if (msg.isEmpty())
        	return;
        
        logger.log(Level.SEVERE, String.format("[%s] %s", ItemDropperPlugin.PLUGIN_NAME, msg), t);
    }
	
	
	public static void info(String msg)
	{
		logger.log(Level.INFO, "[ItemDropper] " + msg);
	}
	
	public static void error(String msg)
	{
		logger.log(Level.SEVERE, "[ItemDropper] Error: " + msg);
	}
	
	
	
    public static String nameTreat(String s) {
        if (s.length() == 0)
            return s;

        s = s.replaceAll("_", " ");
        return s.toLowerCase();
    }
    
    
    
    public static void sendMessage(String message, CommandSender sender)
    {
    	if(sender == null )
    		return;
    		
    	for (String line : message.split("\n"))
    	  sender.sendMessage(line);
    }

//    public static void sendMessage(String node, CommandSender sender, String targetName)
//    {
//        if (sender != null) {
//            String message = getNode(node, sender.getName(), targetName);
//
//            for (String line : message.split("\n"))
//                sender.sendMessage(line);
//        }
//    }

}