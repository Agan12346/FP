package javaapplication1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Dao {
	// instance fields
	static Connection connect = null;
	Statement statement = null;

	// constructor
	public Dao() {
	  
	}

	public Connection getConnection() {
		// Setup the connection with the DB
		try {
			connect = DriverManager
					.getConnection("jdbc:mysql://www.papademas.net:3307/tickets?autoReconnect=true&useSSL=false"
							+ "&user=fp411&password=411");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connect;
	}

	// CRUD implementation

	public void createTables() {
		// variables for SQL Query table creations
		final String createTicketsTable = "CREATE TABLE agan3_tickets(ticket_id INT AUTO_INCREMENT PRIMARY KEY, ticket_issuer VARCHAR(30), ticket_issuer_ID VARCHAR(30), ticket_description VARCHAR(200), time_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, last_updated TIMESTAMP)";
		//final String createUsersTable = "CREATE TABLE agan3_users(uid INT AUTO_INCREMENT PRIMARY KEY, uname VARCHAR(30), upass VARCHAR(30), admin int)";

		try {

			// execute queries to create tables

			statement = getConnection().createStatement();

			statement.executeUpdate(createTicketsTable);
			//statement.executeUpdate(createUsersTable);
			System.out.println("Created tables in given database...");

			// end create table
			// close connection/statement object
			statement.close();
			connect.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		// add users to user table
		addUsers();
	}

	public void addUsers() {
		// add list of users from userlist.csv file to users table

		// variables for SQL Query inserts
		String sql;

		Statement statement;
		BufferedReader br;
		List<List<String>> array = new ArrayList<>(); // list to hold (rows & cols)

		// read data from file
		try {
			br = new BufferedReader(new FileReader(new File("./userlist.csv")));

			String line;
			while ((line = br.readLine()) != null) {
				array.add(Arrays.asList(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("There was a problem loading the file");
		}

		try {

			// Setup the connection with the DB

			statement = getConnection().createStatement();

			// create loop to grab each array index containing a list of values
			// and PASS (insert) that data into your User table
			for (List<String> rowData : array) {

				sql = "insert into agan3_users(uname,upass,admin) " + "values('" + rowData.get(0) + "'," + " '"
						+ rowData.get(1) + "','" + rowData.get(2) + "');";
				statement.executeUpdate(sql);
			}
			System.out.println("Inserts completed in the given database...");

			// close statement object
			statement.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public int insertRecords(String ticketName, String ticketDesc, int UserID) {
		int id = 0;
		try {
			statement = getConnection().createStatement();
	
			statement.executeUpdate("Insert into agan3_tickets (ticket_issuer, ticket_description, ticket_issuer_ID) " + "values('" + ticketName + "', '" + ticketDesc + "', " + UserID + ")", 
					  Statement.RETURN_GENERATED_KEYS);	
			// retrieve ticket id number newly auto generated upon record insertion
			ResultSet resultSet = null;	
			resultSet = statement.getGeneratedKeys();	
			if (resultSet.next()) {	
				// retrieve first field in table		
				id = resultSet.getInt(1);
				statement.executeUpdate("UPDATE agan3_tickets SET last_updated = time_created WHERE ticket_id = " + id); 	
			}	
			} catch (SQLException e) {	
				// TODO Auto-generated catch block		
				e.printStackTrace();	
			}
		return id;
		}

	

	public ResultSet readRecords(boolean isAdmin, int ID) {
		ResultSet results = null;
		try {
			statement = connect.createStatement();			
			if(isAdmin) {
				results = statement.executeQuery("SELECT * FROM agan3_tickets");
			} else {
				results = statement.executeQuery("SELECT * FROM agan3_tickets WHERE ticket_issuer_ID = " + ID);
			}
			//connect.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}
	
	public boolean updateRecords(int id, String column, String newValue, Boolean chkIfAdmin, int userID) {
		  try {		    
		    statement = connect.createStatement();		    
		    if(!chkIfAdmin) {
		    	String sql = "SELECT ticket_issuer_ID FROM tickets WHERE id = " + id;	    	
		    	Statement stmt = connect.createStatement(); 
		    	ResultSet rs = stmt.executeQuery(sql);
		    	String ticketIssuerId = "";
		    	if(rs.next()) {
		    	  ticketIssuerId = rs.getString("ticket_issuer_ID"); 
		    	}
		    	// Compare IDs 
		    	if(ticketIssuerId.equals(userID)) {
				    String updateQuery = "UPDATE agan3_tickets SET " + column + " = '" + newValue + "' WHERE ticket_id = " + id;
					statement.executeUpdate(updateQuery);
					
					String updateTimestampQuery = "UPDATE agan3_tickets SET last_updated = CURRENT_TIMESTAMP " + "WHERE ticket_id = " + id;     
					statement.executeUpdate(updateTimestampQuery);
						    
					return true;
		    	}
		    	else {
		    		return false;
		    	}
		    }

		    String updateQuery = "UPDATE agan3_tickets SET " + column + " = '" + newValue + "' WHERE ticket_id = " + id;
		    statement.executeUpdate(updateQuery);
		    
		    String updateTimestampQuery = "UPDATE agan3_tickets SET last_updated = CURRENT_TIMESTAMP " + "WHERE ticket_id = " + id;
		    statement.executeUpdate(updateTimestampQuery);
		    
		    return true;

		  } catch (SQLException e) {
		    e.printStackTrace();
		    return false;
		  }

		}
	
	
	public boolean deleteRecord(int id, Boolean chkIfAdmin) {
		if(chkIfAdmin) {
		  try {
		    statement = connect.createStatement();
		    String deleteQuery = "DELETE FROM agan3_tickets WHERE ticket_id = " + id;		    
		    statement.executeUpdate(deleteQuery);		    
		    return true;		    
		  	} catch (SQLException e) {
		  		e.printStackTrace();
		  		return false;
		  	}
		} else {
			return false;
		}
	}
	
	public boolean CheckIfValid(String colN) {
		if(colN.equals("ticket_issuer") || colN.equals("ticket_description")){
			return true;
		} else {
			return false;
		}
	}

}
