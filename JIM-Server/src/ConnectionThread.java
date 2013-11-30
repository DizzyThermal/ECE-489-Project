import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.swing.JOptionPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
		thread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					while(!socket.isClosed())
					{
						String clientMessage = bReader.readLine();
						System.out.println(clientMessage);
						if (clientMessage != null)
						{
							JSONObject incomingJSON = null;
							try
							{
								incomingJSON = (JSONObject)(new JSONParser().parse(clientMessage));
							}
							catch(ParseException pe) { pe.printStackTrace(); }
							
							if(((String)incomingJSON.get("action")).equals("connect"))
								connect(id, (String)incomingJSON.get("userName"), (String)incomingJSON.get("password"), socket.getInetAddress().toString().replace("/", ""));
							else if(((String)incomingJSON.get("action")).equals("register"))
								register((String)incomingJSON.get("userName"), (String)incomingJSON.get("password"));
							else if(((String)incomingJSON.get("action")).equals("requestUserList"))
								sendUserList();
							else if(((String)incomingJSON.get("action")).equals("message"))
								relayMessage((int)(long)incomingJSON.get("userId"), (String)incomingJSON.get("userMessage"));
							else if(((String)incomingJSON.get("action")).equals("disconnect"))
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
		if(ip.equals("127.0.0.1"))
		{
			try
			{
				URL whatismyip = new URL("http://checkip.amazonaws.com");
				BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

				ip = in.readLine();
			}
			catch(IOException ioe) { ioe.printStackTrace(); }
		}
		ArrayList<String> dbUsernames = new ArrayList<String>();
		ArrayList<String> dbPasswords = new ArrayList<String>();
		
		try
		{
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:Users.db");
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
		catch (SQLException | ClassNotFoundException e) { e.printStackTrace(); }
		
		for(int i = 0; i < dbUsernames.size(); i++)
		{
			if(dbUsernames.get(i).equals(username))
			{
				if(dbPasswords.get(i).equals(password))
				{
					JSONObject json = new JSONObject();
					json.put("action", "addUser");
					json.put("userId", id);
					json.put("userName", username);

					for(int j = 0; j < Main.userList.size(); j++)
					{
						if(Main.userList.get(j).getName().compareTo(username) > 0)
						{
							Main.userList.add(j, new User(id, username, ip));
							System.out.println("\"" + username + "\" has logged in!");
							
							JSONObject jsonConnected = new JSONObject();
							jsonConnected.put("action", "connected");
							
							writeToClient(jsonConnected.toJSONString());
							Main.writeToAll(json.toJSONString());
							return;
						}
					}
					
					Main.userList.add(new User(id, username, ip));
					System.out.println("\"" + username + "\" has logged in!");
					
					JSONObject jsonConnected = new JSONObject();
					jsonConnected.put("action", "connected");
					
					writeToClient(jsonConnected.toJSONString());
					Main.writeToAll(json.toJSONString());
				}
				else
					writeToClient(makeJSONMessage("The password entered is incorrect!", JOptionPane.ERROR_MESSAGE));
				
				return;
			}
		}
		
		writeToClient(makeJSONMessage("\"" + username + "\" is not registered!", JOptionPane.ERROR_MESSAGE));
	}
	
	public void register(String username, String password) throws ClassNotFoundException
	{
		ArrayList<String> dbUsernames = new ArrayList<String>();
		try
		{
			Class.forName("org.sqlite.JDBC");
			Connection conn = (Connection)DriverManager.getConnection("jdbc:sqlite:Users.db");
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
		
		if(!dbUsernames.contains(username))
		{
			try
			{
				Class.forName("org.sqlite.JDBC");
				Connection conn = (Connection)DriverManager.getConnection("jdbc:sqlite:Users.db");
				Statement stmt = (Statement)conn.createStatement();
				
				String query = "INSERT INTO users VALUES(\"" + username + "\", \"" + password + "\")";
				stmt.executeUpdate(query);
				
				stmt.close();
				conn.close();
			}
			catch(ClassNotFoundException | SQLException e) { e.printStackTrace(); }
			writeToClient(makeJSONMessage(username + " was successfully registered!", JOptionPane.INFORMATION_MESSAGE));
		}
		else
			writeToClient(makeJSONMessage(username + " is already registered!", JOptionPane.ERROR_MESSAGE));
	}
	
	public void sendUserList()
	{
		 JSONArray connectionJSON = new JSONArray();
         for(int i = 0; i < Main.userList.size(); i++)
         {
                 JSONObject JSON = new JSONObject();
                 JSON.put("userId", Main.userList.get(i).getId());
                 JSON.put("userName", Main.userList.get(i).getName());
                 
                 connectionJSON.add(JSON);
         }
         writeToClient(connectionJSON.toJSONString());
	}
	
	public void disconnect(int id)
	{
		for(int i = 0; i < Main.userList.size(); i++)
		{
			if(Main.userList.get(i).getId() == id)
			{
				Main.userList.remove(i);
				
				JSONObject connectionJSON = new JSONObject();
				connectionJSON.put("action", "removeUser");
				connectionJSON.put("userId", id);
				Main.writeToAll(connectionJSON.toJSONString());
				
				thread.stop();
				Main.clientThreads.remove(i);
			}
		}
		
		JSONObject disconnectJSON = new JSONObject();
		disconnectJSON.put("action", "removeUser");
		disconnectJSON.put("userId", id);
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
	
	public void relayMessage(int id, String message)
	{
		for(int i = 0; i < Main.userList.size(); i++)
		{
			if(Main.userList.get(i).getId() == id)
			{
				JSONObject json = new JSONObject();
				json.put("action", "message");
				json.put("userMessage", message);
				json.put("userName", getUserNameById(this.id));
				json.put("senderId", this.id);
				Main.clientThreads.get(getIndexFromId(id)).writeToClient(json.toJSONString());
			}
		}
	}
	
	public String getUserNameById(int id)
	{
		for(int i = 0; i < Main.userList.size(); i++)
		{
			if(Main.userList.get(i).getId() == id)
				return Main.userList.get(i).getName();
		}
		
		return null;
	}
	
	public int getIndexFromId(int id)
	{
		for(int i = 0; i < Main.clientThreads.size(); i++)
		{
			if(Main.clientThreads.get(i).getId() == id)
				return i;
		}
		
		return -1;
	}
	
	public String makeJSONMessage(String message, int messageType)
	{
		JSONObject jMessage = new JSONObject();
		jMessage.put("action", "message");
		jMessage.put("serverMessage", message);
		jMessage.put("type", Integer.toString(messageType));
		
		return jMessage.toJSONString();
	}
	
	public int getId() { return id; }
}
