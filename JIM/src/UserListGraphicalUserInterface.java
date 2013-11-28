import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserListGraphicalUserInterface extends JFrame implements ActionListener, MouseListener, WindowListener
{
	public JPanel usersPanel = new JPanel(new BorderLayout());
	public JScrollPane users = new JScrollPane(usersPanel);
	public ArrayList<JLabel> userLabels = new ArrayList<JLabel>();
	
	// NJP+{ Adding for temp debug
	public JLabel tempLabel = new JLabel("Port of Client"); 
	public JTextField remoteClientPort = new JTextField();
	public JButton connectToClient = new JButton("Connect");
	public JPanel tempRemoteClient = new JPanel();
	// NJP+}
	
	public ArrayList<ChatWindowGraphicalUserInterface> connectedUsers = new ArrayList<ChatWindowGraphicalUserInterface>();
	public ArrayList<User> userList = new ArrayList<User>();
	
	public SSLSocket clientSocket;
	public BufferedWriter bWriter;
	public BufferedReader bReader;
	public ArrayList<Integer> portList = new ArrayList<Integer>();
	
	public Thread t1;
	
	public int id = -1;
	
	UserListGraphicalUserInterface(boolean registering)
	{
		super("ECE 489 - JIM (" + Resource.VERSION_NUMBER + " - " + Resource.VERSION_CODENAME + ")");
		setLayout(new BorderLayout());
		
		//((JTextArea)((JViewport)users.getComponent(0)).getView()).setEditable(false);
		add(users, BorderLayout.CENTER);
		// NJP+{ // temp debug
		tempRemoteClient.add(tempLabel);
		remoteClientPort.setPreferredSize(new Dimension(50,20));
		tempRemoteClient.add(remoteClientPort);
		connectToClient.addActionListener(this);
		tempRemoteClient.add(connectToClient);
		add(tempRemoteClient, BorderLayout.SOUTH);
		// NJP+}
		JSONObject connectionJSON = new JSONObject();
		connectionJSON.put("source", "client");
		if(registering)
			connectionJSON.put("action", "register");
		else
			connectionJSON.put("action", "connect");
		connectionJSON.put("userName", Resource.USERNAME);
		connectionJSON.put("password", DigestUtils.md5Hex(Resource.PASSWORD));

		this.addWindowListener(this);
		
		System.out.println("Before Try");
		try
		{
			clientSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(Resource.IP, Integer.parseInt(Resource.PORT));
			clientSocket.setEnabledCipherSuites(clientSocket.getSupportedCipherSuites());
			bWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			System.out.println(clientSocket.isConnected());
			System.out.println("Before print: "+connectionJSON.toJSONString());
			System.out.println(clientSocket.getOutputStream());
			bWriter.write(connectionJSON.toJSONString() + "\n");
			bWriter.flush();
			System.out.println("Just wrote to buffer");
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
					
					if ((incomingMessage != null) && !incomingMessage.equals("") && !incomingMessage.equals("[]"))
					{
						JSONObject incomingJSON = null;
						JSONArray incomingJSONArray = null;
						System.out.println(incomingMessage);
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
							if(incomingJSON.get("source").equals("server"))
							{
								String action = (String)incomingJSON.get("action");
								if(action.equals("addUser"))
									addUser(Integer.parseInt((String)incomingJSON.get("userId")), (String)incomingJSON.get("userName"), (String)incomingJSON.get("userIp"));
								else if(action.equals("removeUser"))
									removeUser(Integer.parseInt((String)incomingJSON.get("userId")));
								else if(action.equals("message"))
									JOptionPane.showMessageDialog(null, (String)incomingJSON.get("serverMessageTitle"), (String)incomingJSON.get("serverMessage"), JOptionPane.DEFAULT_OPTION);
								else if(action.equals("register")){
									if(!incomingJSON.get("result").equals("success"))
										System.exit(1);
								}
							}
							/*
							else if(incomingJSON.get("source").equals("client_port")){
								// Current connections have already been checked. 
								// Need to create a new connection/window
								ChatWindowGraphicalUserInterface cwGUI = new ChatWindowGraphicalUserInterface(
										Integer.parseInt((String)incomingJSON.get("userId")), 
										getUserNameById(Integer.parseInt((String)incomingJSON.get("userId"))), 
										getIpById(Integer.parseInt((String)incomingJSON.get("userId"))), 
										Integer.parseInt((String)incomingJSON.get("port"))
										);
								cwGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
								cwGUI.setSize(300, 600);
								cwGUI.setResizable(true);
								cwGUI.setVisible(true);	
								connectedUsers.add(cwGUI);
							}
							*/
							/*
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
							*/	
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
			JLabel jL = new JLabel(username);
			jL.addMouseListener(this);
			userLabels.add(index, jL);
			usersPanel.add(jL, index);
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
			userLabels.remove(index);
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
			jL.addMouseListener(this);
			userLabels.add(jL);
			usersPanel.add(jL);
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
		System.out.println("Sending Disconnect to Server.");
		JSONObject json = null;
		if (bWriter != null){
			json = new JSONObject();
			json.put("source", "client");
			json.put("action", "disconnect");
			try {
				bWriter.write(json.toJSONString()+"\n");
				bWriter.flush();
			} catch (IOException e1) { e1.printStackTrace(); }
		}
		t1.stop();
		try
		{
			bReader.close();
			bWriter.close();
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

	// get random port above 50,000
	public int getRandomPort(){
		int randomPort = (int)(Math.random()/2*100000)+50000;
		while(portList.contains(randomPort)){
			randomPort = (int)(Math.random()/2)*100000+50000;
		}
		portList.add(randomPort);
		return randomPort;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == connectToClient){
			connectToUser(12345);
			return;
		}
		// Check if we are already connected
		ChatWindowGraphicalUserInterface tmp = (ChatWindowGraphicalUserInterface)e.getSource();
		if(checkConnection(tmp.id) != -1){
			System.out.println("Already connected to user "+tmp.id);
		}
		else{
			connectToUser(tmp.id);
		}
	}
	
	public void connectToUser(int id){
		// Process
		// 1. Client gets random port #
		// 2. Client creates server socket on port
		// 3. client sends link request to server with port# and remote client id
		// 4. Server sends client connection_request with port# and requester client id
		// 5. Client creates client socket
		// 6. P2P connection established
		
		// 1. Client gets random port #
		int port = getRandomPort();
		
		// 2. Client creates server socket on port
		ChatWindowGraphicalUserInterface cwGUI = new ChatWindowGraphicalUserInterface(
				id, 
				getUserNameById(id), 
				getIpById(id), 
				port,
				Resource.SERVER
				);
		cwGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cwGUI.setSize(300, 600);
		cwGUI.setResizable(true);
		cwGUI.setVisible(true);	
		connectedUsers.add(cwGUI);
		
		// 3. client sends link request to server with port# and remote client id
		
		JSONObject json = new JSONObject();
		json.put("source", "client");
		json.put("action", "link");
		json.put("remoteUserId", id);
		json.put("port", getRandomPort());
		System.out.println(json.toJSONString());
		try {
			bWriter.write(json.toJSONString()+"\n");
			bWriter.flush();
		} catch (IOException e) { e.printStackTrace(); }

	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		// TODO - Open Chat
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
