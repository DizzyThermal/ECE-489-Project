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