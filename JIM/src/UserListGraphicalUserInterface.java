import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
	public JPanel usersPanel = new JPanel(new BorderLayout());
	public JScrollPane users = new JScrollPane(usersPanel);
	public ArrayList<JTextField> userTextFields = new ArrayList<JTextField>();
	
	public ArrayList<ChatWindowGraphicalUserInterface> connectedUsers = new ArrayList<ChatWindowGraphicalUserInterface>();
	public ArrayList<User> userList = new ArrayList<User>();
	
	public SSLSocket clientSocket;
	public PrintWriter pWriter;
	public BufferedReader bReader;

	public Thread t1;
	
	public int id = -1;
	
	UserListGraphicalUserInterface(boolean registering)
	{
		super("ECE 489 - JIM (" + Resource.VERSION_NUMBER + " - " + Resource.VERSION_CODENAME + ")");
		setLayout(new BorderLayout());
		
		//((JTextArea)((JViewport)users.getComponent(0)).getView()).setEditable(false);
		add(users, BorderLayout.CENTER);

		JSONObject connectionJSON = new JSONObject();
		connectionJSON.put("source", "client");
		if(registering)
			connectionJSON.put("action", "register");
		else
			connectionJSON.put("action", "connect");
		connectionJSON.put("username", Resource.USERNAME);
		connectionJSON.put("password", DigestUtils.md5Hex(Resource.PASSWORD));

		System.out.println("Before Try");
		try
		{
			clientSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(Resource.IP, Integer.parseInt(Resource.PORT));
			pWriter = new PrintWriter(clientSocket.getOutputStream(), true);
			bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			System.out.println(clientSocket.isConnected());
			System.out.println("Before print: "+connectionJSON.toJSONString());
			System.out.println(clientSocket.getOutputStream());
			pWriter.println(connectionJSON.toJSONString());
			System.out.println("Just wrote to buffer");
			
		}
		catch (Exception e) { e.printStackTrace(); }

		System.out.println("Before thread create");
		t1 = (new Thread()
		{
			@Override
			public void run()
			{
				while(this.isAlive())
				{
					System.out.println("I am alive.");
					String incomingMessage = "";
					try
					{
						if(bReader != null && bReader.ready())
							incomingMessage = bReader.readLine();
					}
					catch(Exception e) { e.printStackTrace(); }
					
					System.out.println(incomingMessage);
					if(!incomingMessage.equals(""))
					{
						System.out.println(incomingMessage);
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
							else if(action.equals("message"))
								JOptionPane.showMessageDialog(null, (String)incomingJSON.get("serverMessageTitle"), (String)incomingJSON.get("serverMessage"), JOptionPane.DEFAULT_OPTION);
						}
						else if(incomingJSON.get("source").equals("client"))
						{
							int clientIndex = checkConnection(Integer.parseInt((String)incomingJSON.get("userId"))); 
							if(clientIndex < 0)
							{
								ChatWindowGraphicalUserInterface cwGUI = new ChatWindowGraphicalUserInterface(Integer.parseInt((String)incomingJSON.get("userId")), getUserNameById(Integer.parseInt((String)incomingJSON.get("userId"))), getIpById(Integer.parseInt((String)incomingJSON.get("userId"))), (String)incomingJSON.get("userMessage"));
								cwGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
								cwGUI.setSize(300, 600);
								cwGUI.setResizable(true);
								cwGUI.setVisible(true);
							}
							else
							{
								connectedUsers.get(clientIndex).append((String)incomingJSON.get("message"));
							}
						}
					}
					else{
						System.out.println("User List Graphical User Interface will not be loaded.");
						return;
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
	
	public int checkConnection(int id)
	{
		for(int i = 0; i < connectedUsers.size(); i++)
		{
			if(connectedUsers.get(i).getId() == id)
				return i;
		}
		
		return -1;
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

	public String getUserNameById(int id)
	{
		for(int i = 0; i < userList.size(); i++)
		{
			if(userList.get(i).getId() == id)
			{
				return userList.get(i).getName();
			}
		}
		
		return null;
	}
	
	public String getIpById(int id)
	{
		for(int i = 0; i < userList.size(); i++)
		{
			if(userList.get(i).getId() == id)
			{
				return userList.get(i).getIp();
			}
		}
		
		return null;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		
	}
}
