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
	
	public static final String TABLE_NAME = "cenas";
	
	private boolean isClosed = false;
	
	
	
	public DB(ItemDropperPlugin plugin)
	{
		this.plugin = plugin;
	}
	
	public boolean init() throws SQLException
	{
		String host = plugin.getConfig().getString("mysql.host"),
			   db = plugin.getConfig().getString("mysql.db");
			   
		int port = plugin.getConfig().getInt("mysql.port");
		
		username = plugin.getConfig().getString("mysql.username");
		password = plugin.getConfig().getString("mysql.password");
		//tablePrefix = plugin.getConfig().getString("mysql.prefix");
		
		if(host == null || username == null || password == null)
		{
			Utils.error("Database configurations not found.");
			return false;
		}
		
		if( port <= 0 )
			port = 3306;
		
        url = "jdbc:mysql://" + host + ":" + port + "/" + db + "";
        
//        if( tablePrefix == null )
//        	tablePrefix = "";
        
        configured = true;
        
        connect();
        
        try {
        	createTableIfNotExists();
        	
        } catch(SQLException e) {
        	Utils.severe("Could not create table in database");
        	
        	throw e;
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
			//stmt.setString(1, tablePrefix + name);
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
        if( !tableExists(TABLE_NAME) )
        {
        	StringBuilder sb = new StringBuilder();
        	sb.append("CREATE TABLE `");
        	
//        	if(tablePrefix.length() > 0 )
//        		sb.append(tablePrefix);
        	
        	sb.append(TABLE_NAME);
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
			
			PreparedStatement stmt = prepare( sb.toString() );
			stmt.executeUpdate();
			close(stmt);
				
        }
	}
	
    public static void close(PreparedStatement stmt) throws SQLException
    {
    	if( stmt != null )
    		stmt.close();
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
