
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
	private final static String url = "jdbc:sqlite:library.db";

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


//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class Main {
//	public static void main(String[] args) {
//		try {
//			String Url = "jdbc:sqlserver://DESKTOP-CI6OH6R;database=sangha_karan_db;integratedSecurity=true;encrypt=false";
//			Connection conn = DriverManager.getConnection(Url);		
//			System.out.println("Connection Success");
//		}catch(SQLException e) {
//			System.out.println("Connection Failed");
//			e.printStackTrace();
//		}
//	}
//}