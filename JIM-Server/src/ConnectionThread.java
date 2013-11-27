import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.net.ssl.SSLSocket;

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
						//if(bReader.ready())
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
									connect(id, (String)incomingJSON.get("userName"), socket.getInetAddress().toString());
								else if(action.equals("register"))
									register(id, (String)incomingJSON.get("userName"), socket.getInetAddress().toString());
								else if(action.equals("disconnect"))
									disconnect(id, (String)incomingJSON.get("userName"), socket.getInetAddress().toString());
							}
						}
					}
				}
				catch (Exception e) { e.printStackTrace(); }
			}
		};
		thread.start();
	}
	
	public void connect(int id, String username, String ip)
	{
		// Check DB
		
		// Send Userlist if Connected
		// TODO
		//
		// ELSE - Message Error
		writeToClient("There was an error while attempting to login to the server!");
	}
	
	public void register(int id, String username, String ip) throws ClassNotFoundException
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = (Connection)DriverManager.getConnection("jdbc:mysql://localhost:3306/ece489project", Resource.MYSQL_USER, Resource.MYSQL_PASS);
			Statement stmt = (Statement)conn.createStatement();
			
			String query = "SELECT * FROM users;";
			ResultSet rs = stmt.executeQuery(query);
			System.out.println("Hey");
		}
		catch(ClassNotFoundException | SQLException e) { e.printStackTrace(); }
		
		//if()
			writeToClient(username + " was successfully registered!");
		//else
			writeToClient(username + " is already registered!");
	}
	
	public void disconnect(int id, String username, String ip)
	{
		
	}
	
	public void writeToClient(String message)
	{
		try {
			bWriter.write(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getId() { return id; }
}