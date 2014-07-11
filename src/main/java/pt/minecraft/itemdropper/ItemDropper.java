package pt.minecraft.itemdropper;

//import org.bukkit.Bukkit;
//import org.bukkit.Material;
//import org.bukkit.plugin.java.JavaPlugin;
//import org.bukkit.scheduler.BukkitRunnable;
//import org.bukkit.entity.Player;
//import org.bukkit.entity.HumanEntity;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.PlayerInventory;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.HashMap;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Timestamp;
//
//import de.luricos.bukkit.xAuth.database.Table;
//import de.luricos.bukkit.xAuth.utils.xAuthLog;
//import de.luricos.bukkit.xAuth.xAuth;
//import de.luricos.bukkit.xAuth.xAuthPlayer;
//import de.luricos.bukkit.xAuth.MessageHandler;


public class ItemDropper
{
//    private final xAuth plugin;
//    private ItemDropperPoller poller;
//
//    public ItemDropper(final xAuth plugin)
//    {
//        this.plugin = plugin;
//        this.poller = new ItemDropperPoller(plugin);
//        this.poller.runTaskAsynchronously(plugin);
//    }
//
//    public void cancel()
//    {
//        // XXX
//        this.poller.cancel();
//        super.cancel();
//    }
//
//    public void takeDrop(int id)
//    { 
//        Connection conn = plugin.getDatabaseController().getConnection();
//        PreparedStatement ps = null;
//
//        try
//        {
//            String sql = String.format("UPDATE `%s` SET `takendate` = ? WHERE `id` = ?",
//                    plugin.getDatabaseController().getTable(Table.ITEMDROPS));           
//            ps = conn.prepareStatement(sql);
//            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
//            ps.setInt(2, id);
//            ps.executeUpdate();
//        }
//        catch (SQLException e)
//        {
//            xAuthLog.severe("Failed to process item drops.", e);
//        }
//        finally
//        {
//            plugin.getDatabaseController().close(conn, ps);
//        }
//    }
//
//    public void deliverDrop(xAuthPlayer xp, ItemDrop d)
//    { 
//        Player p =  xp.getPlayer();
//        ItemStack is = new ItemStack(d.getItem(), d.getSize(), d.getItemAux());
//        MessageHandler mh = plugin.getMessageHandler();
//        String iname = NameTreater.treat(Material.getMaterial(d.getItem()).name());
//        HashMap<Integer, ItemStack> leftOver = new HashMap<Integer, ItemStack>();
//
//        leftOver.putAll((p.getInventory().addItem(is)));
//        mh.sendMessage(
//            String.format(
//                mh.getNode("itemdropper.delivered"),
//                Integer.toString(d.getSize()), iname), 
//            p);
//
//        if (!leftOver.isEmpty()) {
//
//            Iterator iter = leftOver.keySet().iterator();
//            while(iter.hasNext()) {
//                Integer key = (Integer)iter.next();
//                ItemStack val = (ItemStack)leftOver.get(key);
//                p.getWorld().dropItemNaturally(p.getEyeLocation(), val);
//            }
//
//
//            mh.sendMessage(mh.getNode("itemdropper.dropped"), p);
//        }
//    }
//
//    public void run()
//    {
//        ArrayList<ItemDrop> drops = poller.pickItemDrops();
//        if (drops == null)
//            return;
//
//        for (ItemDrop d : drops)
//        {
//            xAuthPlayer xp = plugin.getPlayerManager().getPlayerById(
//                    d.getAccountId());
//
//            if (xp != null
//                && xp.isOnline()
//                && xp.getStatus() == xAuthPlayer.Status.AUTHENTICATED)
//            {
//                deliverDrop(xp, d);
//                takeDrop(d.getId());
//            }    
//        }
//    }
}

//