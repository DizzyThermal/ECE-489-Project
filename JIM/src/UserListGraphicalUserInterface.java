import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserListGraphicalUserInterface extends JFrame implements MouseListener, WindowListener
{
	public JPanel usersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	public JScrollPane users = new JScrollPane(usersPanel);
	public ArrayList<JLabel> userLabels = new ArrayList<JLabel>();
	
	public static ArrayList<ChatWindowGraphicalUserInterface> connectedUsers = new ArrayList<ChatWindowGraphicalUserInterface>();
	public static ArrayList<User> userList = new ArrayList<User>();
	
	public SSLSocket clientSocket;
	public static SSLServerSocket listeningSocket;

	public static BufferedWriter bWriter;
	public BufferedReader bReader;
	public ArrayList<Integer> portList = new ArrayList<Integer>();
	
	public Thread t1;
	public Thread t2;
	
	public int id = -1;
	
	public boolean connected = false;
	
	UserListGraphicalUserInterface(final SSLSocket clientSocket)
	{
		super("JIM (" + Resource.VERSION_NUMBER + " - " + Resource.VERSION_CODENAME + ")");
		this.clientSocket = clientSocket;
		
		initGUI();
		
		try
		{
			bWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			
			JSONObject jsonUserList = new JSONObject();
			jsonUserList.put("action", "requestUserList");
			
			bWriter.write(jsonUserList.toJSONString() + "\n");
            bWriter.flush();
		}
 		catch (Exception e) { e.printStackTrace(); }

		t1 = (new Thread()
		{
			@Override
			public void run()
			{
				while(!clientSocket.isClosed())
				{
					String incomingMessage = null;
					try
					{
						incomingMessage = bReader.readLine();
					}
					catch(IOException ioe) { ioe.printStackTrace(); }
					if(incomingMessage == null || incomingMessage.equals("") || incomingMessage.equals("[]"))
						continue;
					
					// We Got Something!
					JSONObject incomingJSON = null;
					JSONArray incomingJSONArray = null;
					try
					{
						if((incomingMessage.charAt(0) == '[') && (incomingMessage.charAt(incomingMessage.length()-1) == ']'))
							incomingJSONArray = (JSONArray)(new JSONParser().parse(incomingMessage));
						else
							incomingJSON = (JSONObject)(new JSONParser().parse(incomingMessage));
					}
					catch(ParseException pe) { pe.printStackTrace(); }
					
					if(incomingJSONArray != null)
						updateUsers(incomingJSONArray);
					else if(incomingJSON != null)
					{
						if(((String)incomingJSON.get("action")).equals("addUser"))
							addUser((int)(long)incomingJSON.get("userId"), (String)incomingJSON.get("userName"));
						else if(((String)incomingJSON.get("action")).equals("removeUser"))
							removeUser(Integer.parseInt((String)incomingJSON.get("userId")));
						else if(((String)incomingJSON.get("action")).equals("message"))
						{
							int clientId = checkConnection((int)(long)incomingJSON.get("userId"));
							if(clientId >= 0)
								connectedUsers.get(clientId).append((String)incomingJSON.get("userMessage"));
							else
								connectedUsers.add(new ChatWindowGraphicalUserInterface((int)(long)incomingJSON.get("senderId"), (String)incomingJSON.get("userName"), (String)incomingJSON.get("userMessage")));
						}
					}
				}
			}
		});
		t1.start();
	}
	
	public void initGUI()
	{
		usersPanel.setPreferredSize(new Dimension(225, 550));
		setResizable(false);
		add(users);
		addWindowListener(this);
	}
	
	public static int checkConnection(int id)
	{
		for(int i = 0; i < connectedUsers.size(); i++)
		{
			if(connectedUsers.get(i).getId() == id)
				return i;
		}
		
		return -1;
	}
	
	public void addUser(int id, String username)
	{
		if(username.equals(Resource.USERNAME))
			return;
		
		int index = -1;
		
		for(int i = 0; i < userList.size(); i++)
		{
			if(username.compareTo(userList.get(i).getName()) > 0)
			{
				index = i;
				break;
			}
		}
		
		if(index >= 0)
		{
			userList.add(index, new User(id, username));
			JLabel jL = new JLabel(username);
			jL.setPreferredSize(new Dimension(200, 25));
			jL.addMouseListener(this);
			userLabels.add(index, jL);
			usersPanel.add(jL, index);
		}
		else
		{
			userList.add(new User(id, username));
			JLabel jL = new JLabel(username);
			jL.setPreferredSize(new Dimension(200, 25));
			jL.addMouseListener(this);
			userLabels.add(jL);
			usersPanel.add(jL);
		}
		
		validate();
		repaint();
	}
	
	public void removeUser(int id)
	{
		for(int i = 0; i < userList.size(); i++)
		{
			if(userList.get(i).getId() == id)
			{
				userList.remove(i);
				usersPanel.remove(i);
				userLabels.remove(i);
			}
		}
	}
	
	public void updateUsers(JSONArray json)
	{
		usersPanel.removeAll();

		for(int i = 0; i < json.size(); i++)
		{
			JSONObject jsonObj = (JSONObject)json.get(i);
			
			if(jsonObj.get("userName").equals(Resource.USERNAME))
				continue;
			
			userList.add(new User((int)(long)jsonObj.get("userId"), (String)jsonObj.get("userName")));
			JLabel jL = new JLabel((String)jsonObj.get("userName"));
			jL.setPreferredSize(new Dimension(200, 25));
			jL.addMouseListener(this);
			userLabels.add(jL);
			usersPanel.add(jL);
		}
	}
	
	public void disconnect()
	{
		try {
			JSONObject json = new JSONObject();
			json.put("action", "disconnect");
			bWriter.write(json.toJSONString() + "\n");
			bWriter.flush();
			
			bReader.close();
			bWriter.close();
			clientSocket.close();
		} catch (IOException e1) { e1.printStackTrace(); }

		setVisible(false);
		dispose();
		
		t1.stop();
		System.exit(0);
	}
	
	public static void sendMessageToServer(int id, String message)
	{
		JSONObject json = new JSONObject();
		json.put("action", "message");
		json.put("userId", id);
		json.put("userMessage", message);
		
		try
		{
			bWriter.write(json.toJSONString() + "\n");
	        bWriter.flush();
		}
		catch (IOException ioe) { ioe.printStackTrace(); }
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		for(int i = 0; i < usersPanel.getComponentCount(); i++)
		{
			if(userLabels.get(i) == e.getSource())
			{
				if(checkConnection(userList.get(i).getId()) < 0)
					connectedUsers.add(new ChatWindowGraphicalUserInterface(userList.get(i).getId(), userList.get(i).getName(), null));
			}
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {
		disconnect();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}
}
