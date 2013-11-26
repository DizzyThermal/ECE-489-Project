import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ConnectionThread
{
	Thread thread;
	BufferedReader bReader;
	PrintWriter pWriter;
	int id = -1;
	
	public ConnectionThread(final int id, final SSLSocket socket, final String ip)
	{
		this.id = id;
		try
		{
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pWriter = new PrintWriter(socket.getOutputStream(), true);
		}
		catch(Exception e) { e.printStackTrace(); }
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
						//System.out.println("Inside Socket While loop");
						if(bReader.ready())
						{
							System.out.println("Breader is ready");
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
	
	public void register(int id, String username, String ip)
	{
		// Check DB for existing username
		
		// Add User and Prompt about successful login
		// TODO
		writeToClient(username + " was successfully registered!");
		//
		// ELSE - User Exists
		writeToClient(username + " is already registered!");
	}
	
	public void disconnect(int id, String username, String ip)
	{
		
	}
	
	public void writeToClient(String message)
	{
		pWriter.println(message);
	}
	
	public int getId() { return id; }
}