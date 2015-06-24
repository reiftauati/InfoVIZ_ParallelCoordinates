package parallel_coords1;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Model {
	private String dbName, /*tableName,*/ driver, protocol;
	private Connection conn;
	private boolean badConnection;

	public Model() {
		connectToDatabase();
		//disconnectFromDB();
	}

	public void connectToDatabase() {
		protocol = "jdbc:derby";
		driver = "org.apache.derby.jdbc.EmbeddedDriver";
		dbName = "pollster";				
		//		tableName = "cis";
		//		username = "poll";
		//		passwd = "poll";

		//		try {
		//			// The newInstance() call is a workaround for some
		//			// broken Java implementations
		//			Class.forName(driver).newInstance();
		//		} catch (Exception ex) {
		//			// handle the error
		//			Main.say("Error loading " + driver);
		//		}
		try {
			conn = DriverManager.getConnection(protocol + ":"
					+ dbName);
			if (conn!= null && !conn.isClosed()) {
				conn.setReadOnly(true);
				Main.say("Successfully connected to database " +dbName + " using driver " + driver);
				badConnection = false;
				//				int rowCount = performCountQuery("SELECT COUNT(*) FROM " + tableName);
				//				Main.say("there are " + rowCount + " rows in " + tableName);
			}
		} catch (SQLException ex) {
			badConnection = true;
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public void disconnectFromDB() {
		if (conn != null) {
			try {
				conn.close();
				Main.say("Successfully disconnected from database");
			} catch (SQLException e) {
				Main.say("ERROR disconnecting from database!");
			}
		}
	}

	public int performCountQuery(String q) {
		int result = -1;
		try {
			if (conn != null && !conn.isClosed()) {
				//test
				Statement stmt = null;
				ResultSet rs = null;
				stmt = conn.createStatement();
				if (stmt != null) {
					rs = stmt.executeQuery(q);
					if (rs != null) {
						rs.next();//rs.first();
						result = rs.getInt(1);
						rs.close();
					}
					stmt.close();
				}
			}
		} catch (SQLException ex) {
			Main.say("Error executing query: " + q);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return result;
	}

	public void perform2ColumnQuery(String q, List<String> labels, List<Double> values) {
		try {
			if (conn != null && !conn.isClosed()) {
				Statement stmt = null;
				ResultSet rs = null;
				stmt = conn.createStatement();
				if (stmt != null) {
					rs = stmt.executeQuery(q);
					if (rs != null) {
						while (rs.next()) {
							values.add(rs.getDouble(1));
							labels.add(rs.getString(2));
						}
					}
					rs.close();
					stmt.close();
				}
			}
		} catch (SQLException ex) {
			Main.say("Error executing query: " + q);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
	}

	public List<Axis> performReconnaissanceQuery(String table) {
		List<Axis> result = new ArrayList<Axis>();
		//String q = "DESCRIBE " + table;
		try {
			if (conn != null && !conn.isClosed()) {
				//test
				DatabaseMetaData meta = null;
				ResultSet rs = null;
				meta = conn.getMetaData();
				if (meta != null) {
					rs = meta.getColumns(null, null, null, null);
					while (rs.next()) {
						if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
							String col = rs.getString("COLUMN_NAME");
							String type = rs.getString("TYPE_NAME");
							System.out.println("col=" + col + "; type="+type);
							result.add(new Axis(col, type));
						}
					}
					rs.close();
				}
			}
		} catch (SQLException ex) {
			Main.say("Error fetching metadata for table: " + table);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return result;
	}

	public void shutdown() {
		disconnectFromDB();		
	}

	public int performFullQuery(String table, List<Axis> axes) {
		String q = "SELECT * FROM " + table;
		int rows = 0;
		try {
			if (conn != null && !conn.isClosed()) {
				//test
				Statement stmt = null;
				ResultSet rs = null;
				stmt = conn.createStatement();
				if (stmt != null) {
					rs = stmt.executeQuery(q);
					while (rs.next()) {
						++rows;
						for (Axis a : axes) {
							a.extractData(rs);
						}
					}
					rs.close();
					stmt.close();
				}
			}
		} catch (SQLException ex) {
			Main.say("Error executing query: " + q);
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		//set scale for each axis
		for (Axis a : axes) {
			a.normalize();
		}
		return rows;
	}


}
