import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

public class ConnectionThread 	 
{
	Thread thread;
	BufferedReader bReader;
	BufferedWriter bWriter;
	int id = -1;
	
	public ConnectionThread(final int id, final SSLSocket socket, final String ip)
	{
		this.id = id;
		try
		{
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			bWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		}
		catch(Exception e) { e.printStackTrace(); }
		socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
		System.out.println("Socket: "+socket.isConnected());
		System.out.println(bReader);
		thread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					System.out.println(socket.getInetAddress() + ":" + socket.getPort() + " connected!");
					
					while(!socket.isClosed())
					{
						String clientMessage = bReader.readLine();
						if (clientMessage != null)
						{
							JSONObject incomingJSON = null;
							try
							{
								incomingJSON = (JSONObject)(new JSONParser().parse(clientMessage));
							}
							catch(ParseException pe) { pe.printStackTrace(); }
							
							String action = (String)incomingJSON.get("action");
							if(action.equals("connect"))
								connect(id, (String)incomingJSON.get("userName"), (String)incomingJSON.get("password"), socket.getInetAddress().toString());
							else if(action.equals("register"))
								if(register((String)incomingJSON.get("userName"), (String)incomingJSON.get("password")))
									connect(id, (String)incomingJSON.get("userName"), (String)incomingJSON.get("password"), socket.getInetAddress().toString());
							else if(action.equals("disconnect"))
								disconnect(id);
						}
					}
				}
				catch (Exception e) { e.printStackTrace(); }
			}
		};
		thread.start();
	}
	
	public void connect(int id, String username, String password, String ip)
	{
		ArrayList<String> dbUsernames = new ArrayList<String>();
		ArrayList<String> dbPasswords = new ArrayList<String>();

		JSONObject json = new JSONObject();
		json.put("source", "server");
		json.put("action", "register");
		
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection)DriverManager.getConnection("jdbc:mysql://localhost:3306/ece489project", Resource.MYSQL_USER, Resource.MYSQL_PASS);
			Statement stmt = (Statement)conn.createStatement();
			
			String query = "SELECT * FROM users;";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				dbUsernames.add(rs.getString("username"));
				dbPasswords.add(rs.getString("password"));
			}
			rs.close();
			stmt.close();
			conn.close();
		}
		catch(ClassNotFoundException | SQLException e) { e.printStackTrace(); }
		
		for(int i = 0; i < dbUsernames.size(); i++)
		{
			if(dbUsernames.get(i).equals(username))
			{
				if(dbPasswords.get(i).equals(password))
				{
					sendUserListToClient();
					sendUserToClients(id, username, ip);
					
					Main.userList.add(new User(id, username, ip));
					System.out.println("\"" + username + "\" has logged in!");
				}
				else
				{
					System.out.print(username+" password was incorrect");
					json.put("result", "fail");
					writeToClient(json.toJSONString()+"\n");
				}
				return;
			}
		}

		System.out.print(username+" was not registered");
		json.put("result", "fail");
		writeToClient(json.toJSONString()+"\n");
	}
	
	public boolean register(String username, String password) throws ClassNotFoundException
	{
		ArrayList<String> dbUsernames = new ArrayList<String>();
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection)DriverManager.getConnection("jdbc:mysql://localhost:3306/ece489project", Resource.MYSQL_USER, Resource.MYSQL_PASS);
			Statement stmt = (Statement)conn.createStatement();
			
			String query = "SELECT username FROM users;";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				dbUsernames.add(rs.getString("username"));			
			}
			rs.close();
			stmt.close();
			conn.close();
		}
		catch(ClassNotFoundException | SQLException e) { e.printStackTrace(); }
		

		JSONObject json = new JSONObject();
		json.put("source", "server");
		json.put("action", "register");
		
		if(!dbUsernames.contains(username))
		{
			try
			{
				Class.forName("com.mysql.jdbc.Driver");
				Connection conn = (Connection)DriverManager.getConnection("jdbc:mysql://localhost:3306/ece489project", Resource.MYSQL_USER, Resource.MYSQL_PASS);
				Statement stmt = (Statement)conn.createStatement();
				
				String query = "INSERT INTO users VALUES(\"" + username + "\", \"" + password + "\")";
				stmt.executeUpdate(query);
				
				stmt.close();
				conn.close();
			}
			catch(ClassNotFoundException | SQLException e) { e.printStackTrace(); }

			json.put("result", "success");
			writeToClient(json.toJSONString()+"\n");
			return true;
		}
		else
		{
			json.put("result", "fail");
			writeToClient(json.toJSONString()+"\n");
			return false;
		}
	}
	
	public void disconnect(int id)
	{
		for(int i = 0; i < Main.userList.size(); i++)
		{
			if(Main.userList.get(i).getId() == id)
			{
				Main.userList.remove(i);
				
				JSONObject connectionJSON = new JSONObject();
				connectionJSON.put("source", "server");
				connectionJSON.put("action", "removeUser");
				connectionJSON.put("userId", id);
				Main.writeToAll(connectionJSON.toJSONString());
				
				thread.stop();
				Main.clientThreads.remove(i);
			}
		}
	}
	
	public void writeToClient(String message)
	{
		try
		{
			bWriter.write(message + "\n");
			bWriter.flush();
		}
		catch(IOException ioe) { ioe.printStackTrace(); }
	}
	
	public void sendUserListToClient()
	{
		JSONArray connectionJSON = new JSONArray();
		for(int i = 0; i < Main.userList.size(); i++)
		{
			JSONObject JSON = new JSONObject();
			JSON.put("userId", Main.userList.get(i).getId());
			JSON.put("userName", Main.userList.get(i).getName());
			JSON.put("userIp", Main.userList.get(i).getIp());
			
			connectionJSON.add(JSON);
		}
		writeToClient(connectionJSON.toJSONString());
	}

	public void sendUserToClients(int id, String uname, String ip)
	{
		JSONObject JSON = new JSONObject();
		JSON.put("source", "server");
		JSON.put("action", "addUser");
		JSON.put("userId", Integer.toString(id));
		JSON.put("userName", uname);
		JSON.put("userIp", ip);
		Main.writeToAll(JSON.toJSONString());
		
	}
	public int getId() { return id; }
}
