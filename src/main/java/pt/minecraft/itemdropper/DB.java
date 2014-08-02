package pt.minecraft.itemdropper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DB {
	
	private ItemDropperPlugin plugin = null;
	private Connection conn = null;
	private String url = null;
	private String username = null;
	private String password = null;
	//private String tablePrefix = null;
	private boolean configured = false;
	
	private String table_name = "itemdrop";
	
	private boolean isClosed = false;
	public static boolean configErrorBefore = false;
	
	
	
	public DB(ItemDropperPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean init(boolean checkCreateTable) throws SQLException
	{
		String host = plugin.getConfig().getString("mysql.host"),
			   db = plugin.getConfig().getString("mysql.db");
		
		boolean enabled = plugin.getConfig().getBoolean("mysql.enabled");
			   
		int port = plugin.getConfig().getInt("mysql.port");
		
		username = plugin.getConfig().getString("mysql.username");
		password = plugin.getConfig().getString("mysql.password");
		table_name = plugin.getConfig().getString("mysql.table");
		
		if( !enabled )
		{
			Utils.error("Configure your database in config.yml and then change enabled to true.");
			
			return false;
		}
		
		if(host == null || username == null || password == null)
		{
			if( !configErrorBefore )
			{
				configErrorBefore = true;
				
				Utils.error("Database configurations not found.");	
			}
			
			return false;
		}
		
		if( port <= 0 )
			port = 3306;
		
		if( table_name == null || table_name.length() == 0 )
			table_name = "itemdrop";
		
        url = "jdbc:mysql://" + host + ":" + port + "/" + db + "";
        
        if(plugin.isDebugMode())
        	Utils.debug("Using database url: '%s'", url);

        
        configured = true;
        
        connect();
        
        if( checkCreateTable )
        {
	        try {
	        	createTableIfNotExists();
	        	
	        } catch(SQLException e) {
	        	Utils.severe("Could not create table in database");
	        	
	        	throw e;
	        }   
        }
		
		return true;
	}
	
	
	public Connection connect() throws SQLException
	{
		if( !configured )
		{
			Utils.warning("tried to connect to database without calling init first");
			
			return null;
		}
		
		if (isConnected())
			return conn;
		
		else
		{
			if (conn != null)
			{
				Utils.warning("unexpectedly disconnected from database");
				conn = null;
			}
			
			conn = DriverManager.getConnection(url, username, password);
			conn.setAutoCommit(true);
			conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			
			Utils.info("connected to database");
		}
		return conn;
	}
	
	public boolean isConnected()
	{
		try {
			return conn != null && !conn.isClosed() && conn.isValid(10);
		} catch (SQLException se) {
			return false;
		}
	}
	
	public boolean tableExists(String name) throws SQLException
	{
		PreparedStatement stmt = null;
		ResultSet rs = null;
		boolean exists = false;
		
		try {
			
			stmt = prepare("SHOW TABLES LIKE ?");
			stmt.setString(1, name);
			
			rs = stmt.executeQuery();
			exists = rs.next();
			
		} finally {
			if( rs != null )
				rs.close();
			close(stmt);
		}
		
		return exists;
	}
	
	public PreparedStatement prepare(String sql) throws SQLException
	{
		return connect().prepareStatement(sql);
	}
	
	
	private void createTableIfNotExists() throws SQLException
	{
        if( !tableExists(table_name) )
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("CREATE TABLE `");
        	
        	sb.append(table_name);
        	sb.append("` ( ");
        	
        	sb.append(" `id` INT UNSIGNED NOT NULL AUTO_INCREMENT, "		);
			sb.append(" `accountid` INT UNSIGNED NOT NULL, "				);
			sb.append(" `itemdrop` INT UNSIGNED NOT NULL, "					);
			sb.append(" `itemaux` INT UNSIGNED NULL, "						);
			sb.append(" `itemnumber` INT UNSIGNED NOT NULL, "				);
			sb.append(" `dropdate` DATETIME NOT NULL, "						);
			sb.append(" `takendate` DATETIME NULL, "						);
			sb.append(" `active` TINYINT(1) UNSIGNED NOT NULL DEFAULT 1, "	);
			sb.append(" PRIMARY KEY (`id`) "								);
			
			sb.append(");");
			
			if( plugin.isDebugMode() )
				Utils.debug("Creating database");
			
			PreparedStatement stmt = prepare( sb.toString() );
			stmt.executeUpdate();
			close(stmt);
				
        }
	}
	
	public String getTableName()
	{
		return table_name;
	}
	
    public static void close(PreparedStatement stmt) throws SQLException
    {
    	if( stmt != null )
    		stmt.close();
    }
    public static void safeClose(PreparedStatement stmt)
    {
    	if( stmt != null )
    	{
			try {
				stmt.close();
			} catch (SQLException e) { }
    	}
    }
    
    public void close()
    {
    	if( isClosed )
    		return;
    	
		try {
	    	if( conn != null )
	    		conn.close();
		} catch (SQLException e) {
		} finally {
    		isClosed = true;
    	}
    }
	
    
    @Override
    protected void finalize() throws Throwable
    {
    	this.close();
    	
    	super.finalize();
    }
	

}
