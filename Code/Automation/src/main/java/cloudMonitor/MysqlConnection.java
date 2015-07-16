import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MysqlConnection {
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/cloudMonitorDB";
	private static final String USER = "root";
	private static final String PASS = "";
	private static final String selectSQL = "SELECT `status` FROM `instanceInfo` WHERE `instanceDNS` = ?;";
	private static final String insertSQL = "INSERT INTO `instanceInfo` (`instanceDNS`, `status`) VALUES (?, ?);";
	private static final String updateSQL = "UPDATE `instanceInfo` SET `status` = ? WHERE `instanceDNS` = ?;";
	
	private static Connection connection = null;
	private static PreparedStatement statement = null;
	
	/**
	 * Contructor - initialize connection
	 */
	public MysqlConnection() {
		// initialize connection
		if (connection == null) {
			try {
				Class.forName(JDBC_DRIVER).newInstance();
				connection = DriverManager.getConnection(DB_URL, USER, PASS);
			} catch (Exception e) {
				Utility.logPrint(e.toString());
			}
		}
	}
	
	/**
	 * This method updates an instance's status according to DNS
	 * @param instanceDNS
	 * @param status
	 * @return success
	 */
	public boolean updateInstanceInfo(String instanceDNS, String status) {
		try {
			// select to see if instance exist
			String instanceStatus = null;
			statement = connection.prepareStatement(selectSQL);
			statement.setString(1, instanceDNS);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				instanceStatus = result.getString(1);
			}
			
			// if instance does not exist: insert
			if (instanceStatus == null) {
				statement = connection.prepareStatement(insertSQL);
				statement.setString(1, instanceDNS);
				statement.setString(2, status);
		        int affectedRows = statement.executeUpdate();
		        if (affectedRows == 1) {
		            return true;
		        }
		    // if instance exist: update
			} else if (instanceStatus.equals(status)){
				return true;
			} else {
				statement = connection.prepareStatement(updateSQL);
				statement.setString(1, status);
				statement.setString(2, instanceDNS);
		        int affectedRows = statement.executeUpdate();
		        if (affectedRows == 1) {
		            return true;
		        }
			}
		} catch (Exception e) {
			Utility.logPrint(e.toString());
		}
        return false;
	}
	
	
	

}
