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
	public JPanel usersPanel = new JPanel(new FlowLayout());
	public JScrollPane users = new JScrollPane(usersPanel);
	public ArrayList<JLabel> userLabels = new ArrayList<JLabel>();
	
	public ArrayList<ChatWindowGraphicalUserInterface> connectedUsers = new ArrayList<ChatWindowGraphicalUserInterface>();
	public static ArrayList<User> userList = new ArrayList<User>();
	
	public SSLSocket clientSocket;
	public static SSLServerSocket listeningSocket;

	public BufferedWriter bWriter;
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
            
            JSONObject jsonPort = new JSONObject();
            jsonPort.put("action", "requestPort");
            
            bWriter.write(jsonPort.toJSONString() + "\n");
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
							addUser((int)(long)incomingJSON.get("userId"), (String)incomingJSON.get("userName"), (String)incomingJSON.get("userIp"));
						else if(((String)incomingJSON.get("action")).equals("removeUser"))
							removeUser(Integer.parseInt((String)incomingJSON.get("userId")));
						else if(((String)incomingJSON.get("action")).equals("port"))
						{
							Resource.LISTENING_PORT = (String)incomingJSON.get("port");
							connected = true;
						}
					}
				}
			}
		});
		t1.start();
		
		while(!connected)
		{
			try { Thread.sleep(1); }
			catch (InterruptedException ie) { ie.printStackTrace(); }
		}

		t2 = (new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					listeningSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(Integer.parseInt(Resource.LISTENING_PORT));
				}
				catch (Exception e) { e.printStackTrace(); }

				System.out.println("Listening on Port: " + Resource.LISTENING_PORT);

				while(true) 
				{
					try
					{
						if(listeningSocket.getInetAddress().toString() != "0.0.0.0/0.0.0.0" && !isConnected(listeningSocket.getInetAddress().toString()))
							connectedUsers.add(new ChatWindowGraphicalUserInterface((SSLSocket)listeningSocket.accept(), getUserNameByIp(listeningSocket.getInetAddress().toString().split("/")[0])));
					}
					catch (Exception e) { e.printStackTrace(); }
				}
			}
		});
		t2.start();
	}
	
	public void initGUI()
	{
		setLayout(new BorderLayout());

		add(users, BorderLayout.CENTER);
		addWindowListener(this);
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
	
	public void addUser(int id, String username, String ip)
	{
		if(username.equals(Resource.USERNAME))
			return;
		
		for(int i = 0; i < userList.size(); i++)
		{
			if(username.compareTo(userList.get(i).getName()) > 0)
			{
				userList.add(i, new User(id, username, ip));
				JLabel jL = new JLabel(username);
				jL.setPreferredSize(new Dimension(25, 200));
				jL.addMouseListener(this);
				userLabels.add(i, jL);
				usersPanel.add(jL, i);
				
				break;
			}
		}	
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
			userList.add(new User((int)(long)jsonObj.get("userId"), (String)jsonObj.get("userName"), (String)jsonObj.get("userIp")));
			JLabel jL = new JLabel((String)jsonObj.get("userName"));
			jL.setPreferredSize(new Dimension(25, 200));
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

	public String getUserNameByIp(String ip)
	{
		for(int i = 0; i < userList.size(); i++)
		{
			if(userList.get(i).getIp().equals(ip))
				return userList.get(i).getName();
		}
		
		return null;
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		for(int i = 0; i < usersPanel.getComponentCount(); i++)
		{
			if(userLabels.get(i) == e.getSource())
			{
				try
				{
					connectedUsers.add(new ChatWindowGraphicalUserInterface((SSLSocket)SSLSocketFactory.getDefault().createSocket(userList.get(i).getIp(), Integer.parseInt(Resource.LISTENING_PORT)), getUserNameByIp(userList.get(i).getIp())));
				}
				catch (NumberFormatException | IOException e1) { e1.printStackTrace(); }
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
