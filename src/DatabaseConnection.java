import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private final static String url = "jdbc:sqlite:database/library.db";

	public static Connection connect() {
		Connection conn = null;
		try {
			conn = DriverManager.getConnection(url);		
			System.out.println("Connection Success");
		}catch(SQLException e) {
			System.out.println("Connection Failed");
			e.printStackTrace();
		}
		return conn;

	}
}
