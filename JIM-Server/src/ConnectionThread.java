import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionThread
{
	Thread thread;
	BufferedReader bReader;
	PrintWriter pWriter;
	String IP = null;
	int ID = -1;
	
	public ConnectionThread(final int id, final Socket socket, final String ip)
	{
		ID = id;
		try
		{
			bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pWriter = new PrintWriter(socket.getOutputStream(), true);
		}
		catch(Exception e) { e.printStackTrace(); }

		thread = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					IP = socket.getInetAddress() + ":" + socket.getPort() + " connected!";
					System.out.println(IP);
					IP = socket.getInetAddress().toString();
					
					while(!socket.isClosed())
					{
						if(bReader.ready())
						{
						String clientMessage = bReader.readLine();
						System.out.println(clientMessage);
						if (clientMessage != null)
						{
							// Replace with JSON stuff
							if(clientMessage.contains("/connected"))
							{
								// Need to Authenticate Password with MySQL Database
								Main.userList.add(new User(id, clientMessage.substring(11), ip));
								pWriter.println("/id " + id);
								
								// JSON to update userlist (add new person)
								Main.writeToAll("/userlist " + Main.getUserList());
							}
							else if(clientMessage.contains("/register"))
							{
								// Need to pass Username and Password to Database to register (if user doesn't already exist)
							}
							else if(clientMessage.contains("/disconnect"))
							{
								// Make Remove String JSON to update everyone's Userlist
								Main.writeToAll("/remove " + Main.removeUser(clientMessage));
								Main.ips.remove(IP);
								thread.stop();
							}
							else
							{
								// Need to have ID of who the recipient is + message (JSON)
								int userId = 0; // This is temporary to make code not error, will be resolved from JSON
								String userMessage = "Temporary Message"; // This is temporary to make code not error, will be resolved from JSON
								
								for(int i = 0; i < Main.userList.size(); i++)
								{
									if(Main.userList.get(i).getId() == userId)
									{
										for(int j = 0; j < Main.clientThreads.size(); j++)
										{
											if(Main.clientThreads.get(j).getId() == userId)
											{
												Main.clientThreads.get(j).writeToClient(userMessage);
											}
										}
									}
								}
							}
						}
					}
					}
				}
				catch (Exception e) { e.printStackTrace(); }
			}
		};
		thread.start();
	}
	
	public void writeToClient(String message)
	{
		pWriter.println(message);
	}
	
	public int getId() { return ID; }
}