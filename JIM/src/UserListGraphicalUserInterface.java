import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserListGraphicalUserInterface extends JFrame implements ActionListener
{
	JPanel usersPanel = new JPanel(new BorderLayout());
	JScrollPane users = new JScrollPane(usersPanel);
	ArrayList<JTextField> userTextFields = new ArrayList<JTextField>();
	
	public static ArrayList<User> userList = new ArrayList<User>();
	
	public static Socket clientSocket;
	public static PrintWriter pWriter;
	public static BufferedReader bReader;

	public Thread t1;
	
	public static int id = -1;
	
	UserListGraphicalUserInterface()
	{
		super("ECE 489 - JIM (" + Resource.VERSION_NUMBER + " - " + Resource.VERSION_CODENAME + ")");
		setLayout(new BorderLayout());
		
		((JTextArea)((JViewport)users.getComponent(0)).getView()).setEditable(false);
		add(users, BorderLayout.CENTER);

		JSONObject connectionJSON = new JSONObject();
		connectionJSON.put("source", "client");
		connectionJSON.put("action", "connect");
		connectionJSON.put("username", Resource.USERNAME);
		connectionJSON.put("password", DigestUtils.md5Hex(Resource.PASSWORD));
		
		try
		{
			clientSocket = new Socket(Resource.IP, Integer.parseInt(Resource.PORT));
			pWriter = new PrintWriter(clientSocket.getOutputStream(), true);
			bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			pWriter.println(connectionJSON.toJSONString());
			
		}
		catch (Exception e) { e.printStackTrace(); }

		t1 = (new Thread()
		{
			@Override
			public void run()
			{
				while(this.isAlive())
				{
					String incomingMessage = "";
					try
					{
						if(bReader != null && bReader.ready())
							incomingMessage = bReader.readLine();
					}
					catch(Exception e) { e.printStackTrace(); }
					
					if(!incomingMessage.equals(""))
					{
						
						JSONObject incomingJSON = null;
						try
						{
							incomingJSON = (JSONObject)(new JSONParser().parse(incomingMessage));
						}
						catch(ParseException pe) { pe.printStackTrace(); }
						
						if(incomingJSON.get("source").equals("server"))
						{
							String action = (String)incomingJSON.get("action");
							if(action.equals("addUser"))
								addUser(Integer.parseInt((String)incomingJSON.get("userId")), (String)incomingJSON.get("userName"), (String)incomingJSON.get("userIp"));
							else if(action.equals("removeUser"))
								removeUser(Integer.parseInt((String)incomingJSON.get("userId")));
							else if(action.equals("updateUsers"))
								updateUsers((String[])incomingJSON.get("userIds"), (String[])incomingJSON.get("userNames"), (String[])incomingJSON.get("userIps"));
						}
						else if(incomingJSON.get("source").equals("client"))
						{
							// Create Windows and Connection if it isn't made
							//
							// ELSE
							//
							// Append to Chat Window of GUI that is open
						}
					}
				}
			}
		});
		t1.start();
	}
	
	public void addUser(int id, String username, String ip)
	{
		int index = -1;
		for(int i = 0; i < userList.size(); i++)
		{
			if(!(username.compareTo(userList.get(i).getName()) > 0))
			{
				index = i;
				break;
			}
		}
		
		if(index > 0)
		{
			userList.add(index, new User(id, username, ip));
			JTextField jTF = new JTextField(username);
			jTF.addActionListener(this);
			userTextFields.add(index, jTF);
			usersPanel.add(jTF, index);
		}
	}
	
	public void removeUser(int id)
	{
		int index = -1;
		for(int i = 0; i < userList.size(); i++)
		{
			if(userList.get(i).getId() == id)
			{
				index = i;
				break;
			}
		}
		
		if(index > 0)
		{
			userList.remove(index);
			usersPanel.remove(index);
			userTextFields.remove(index);
		}
	}
	
	public void updateUsers(String[] ids, String[] usernames, String[] ips)
	{
		usersPanel.removeAll();
		for(int i = 0; i < ids.length; i++)
		{
			userList.add(new User(Integer.parseInt(ids[i]), usernames[i], ips[i]));
			JTextField jTF = new JTextField(usernames[i]);
			jTF.addActionListener(this);
			userTextFields.add(jTF);
			usersPanel.add(jTF);
		}
	}
	
	public void disconnect()
	{
		if (pWriter != null)
			pWriter.println("/disconnect " + id);
		t1.stop();
		try
		{
			bReader.close();
			pWriter.close();
			clientSocket.close();
			
		}
		catch(Exception e) { e.printStackTrace(); }
		
		setVisible(false);
		dispose();
		System.exit(0);
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		
	}
}