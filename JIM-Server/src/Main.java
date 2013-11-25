import java.net.ServerSocket;
import java.util.ArrayList;

public class Main
{
	public static ServerSocket serverSocket;

	public static int userId = 0;
	public static ArrayList<User> userList = new ArrayList<User>();
	public static ArrayList<String> ips = new ArrayList<String>();
	public static ArrayList<ConnectionThread> clientThreads = new ArrayList<ConnectionThread>();
	
	public static void main(String[] args)
	{
		try
		{
			serverSocket = new ServerSocket(Integer.parseInt(Resource.PORT));
		}
		catch (Exception e) { e.printStackTrace(); }
		
		System.out.println("Listening on Port: " + Resource.PORT);

		while(true) 
		{
			try
			{
				if(serverSocket.getInetAddress().toString() != "0.0.0.0/0.0.0.0" && !isConnected(serverSocket.getInetAddress().toString()))
				{
					clientThreads.add(new ConnectionThread(++userId, serverSocket.accept(), serverSocket.getInetAddress().toString().split("/")[0]));
				}
			}
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public static String getUserList()
	{
		// Replace this with MySQL request to pull Users
		String userStr = "";
		
		for(int i = 0; i < userList.size(); i++)
		{
			int id = userList.get(i).getId();
			String name = userList.get(i).getName().trim();

			userStr = userStr + String.valueOf(id) + "\\" + name;
			if((i+1) < userList.size())
				userStr = userStr + "\\";
		}

		// Take MySQL Database Users and form JSON String of all of them
		return userStr;
	}
	
	public static String removeUser(String id)
	{
		// Go through LOGGED in Userlist and remove user (they signed off) - Must be recoded
		for(int i = 0; i < userList.size(); i++)
		{
			if(userList.get(i).getId() == Integer.parseInt(id.split(" ")[1]))
			{
				userList.remove(i);
				break;
			}
		}
		
		return id.split(" ")[1];
	}
	
	public static void writeToAll(String message)
	{
		// Instead of writing to all like a chatroom, we're going to route messages to individual users like AIM
		for(int i = 0; i < clientThreads.size(); i++)
		{
			clientThreads.get(i).writeToClient(message);
		}
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