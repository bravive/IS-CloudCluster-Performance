import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class MysqlConnection {
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://localhost/cloudMonitorDB";
	private static final String USER = "root";
	private static final String PASS = "";
	private static final String insertSQL = "INSERT INTO `instanceInfo` (`instanceDNS`, `status`) VALUES (? , ?);";
	private static final String updateSQL = "UPDATE `instanceInfo` SET `status` = ? WHERE `instanceDNS` = ?";
	
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
			} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This method adds an instance into `instanceInfo` table
	 * @param instanceDNS
	 * @param status
	 * @return
	 */
	public boolean insertInstanceInfo(String instanceDNS, String status) {
		try {
			statement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, instanceDNS);
			statement.setString(2, status);
	        int affectedRows = statement.executeUpdate();
	        if (affectedRows == 1) {
	            return true;
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return false;
	}
	
	/**
	 * This method updates an instance's status according to DNS
	 * @param instanceDNS
	 * @param status
	 * @return
	 */
	public boolean updateInstanceInfo(String instanceDNS, String status) {
		try {
			statement = connection.prepareStatement(updateSQL, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, status);
			statement.setString(2, instanceDNS);
	        int affectedRows = statement.executeUpdate();
	        if (affectedRows == 1) {
	            return true;
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
        return false;
	}
	
	
	

}
