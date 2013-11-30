import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

public class Main
{
	public static SSLServerSocket serverSocket;

	public static int userId = 0;
	public static ArrayList<User> userList = new ArrayList<User>();
	public static ArrayList<ConnectionThread> clientThreads = new ArrayList<ConnectionThread>();
	
	public static void main(String[] args)
	{
		checkForDatabase();
		
		try
		{
			serverSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(Integer.parseInt(Resource.PORT));
		}
		catch (Exception e) { e.printStackTrace(); }

		System.out.println("Listening on Port: " + Resource.PORT);

		while(true) 
		{
			try
			{
				if(serverSocket.getInetAddress().toString() != "0.0.0.0/0.0.0.0" && !isConnected(serverSocket.getInetAddress().toString()))
				{
					clientThreads.add(new ConnectionThread(++userId, (SSLSocket) serverSocket.accept(), serverSocket.getInetAddress().toString().split("/")[0]));
				}
			}
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public static void checkForDatabase()
	{
		if(!(new File(System.getProperty("user.dir") + File.separator + "Users.db")).exists())
		{
			try
			{
				Class.forName("org.sqlite.JDBC");
				Connection conn = DriverManager.getConnection("jdbc:sqlite:Users.db");
				
				Statement stmt = conn.createStatement();
				String sql = "CREATE TABLE users (username VARCHAR(50), password VARCHAR(50))";
				stmt.executeUpdate(sql);
				
				stmt.close();
				conn.close();
			}
			catch (SQLException | ClassNotFoundException e) { e.printStackTrace(); }
		}
	}
	
	public static void writeToAll(String message)
	{
		for(int i = 0; i < clientThreads.size(); i++)
			clientThreads.get(i).writeToClient(message);
	}

	public static boolean isConnected(String ip)
	{
		for(int i = 0; i < userList.size(); i++)
		{
			if(userList.get(i).getIp().equals(ip))
				return true;
		}
		
		return false;
	}
}