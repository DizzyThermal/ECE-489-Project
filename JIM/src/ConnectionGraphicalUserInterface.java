import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ConnectionGraphicalUserInterface extends JFrame implements ActionListener, KeyListener
{
	JPanel mainPanel = new JPanel();

	JLabel titleLabel = new JLabel("Connection Information");
	JLabel nameLabel = new JLabel("Username: ");
	JLabel passwordLabel = new JLabel("Password: ");
	
	JButton loginButton = new JButton("Connect");
	JButton registerButton = new JButton("Register");
	
	JTextField name = new JTextField();
	JPasswordField password = new JPasswordField();
	
	Thread t1 = null;
	
	ConnectionGraphicalUserInterface()
	{
		super("Connection Information");
		initGUI();
	}
	
	public void initGUI()
	{
		FlowLayout fl = new FlowLayout();
		fl.setAlignment(FlowLayout.LEFT);
		setLayout(fl);

		mainPanel.setPreferredSize(new Dimension(300, 410));
		titleLabel.setPreferredSize(new Dimension(300, 20));
		titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
		mainPanel.add(titleLabel);
		
		nameLabel.setPreferredSize(new Dimension(250, 20));
		mainPanel.add(nameLabel);
		name.setPreferredSize(new Dimension(250, 20));
		name.setText(Resource.USERNAME);
		name.addKeyListener(this);
		mainPanel.add(name);
		
		passwordLabel.setPreferredSize(new Dimension(250, 20));
		mainPanel.add(passwordLabel);
		password.setPreferredSize(new Dimension(250, 20));
		password.addKeyListener(this);
		mainPanel.add(password);
		
		mainPanel.add(loginButton);
		mainPanel.add(registerButton);
		mainPanel.addKeyListener(this);
		loginButton.addActionListener(this);
		registerButton.addActionListener(this);
		
		add(mainPanel);
	}
	
	public void connect()
	{
		JSONObject json = new JSONObject();
		json.put("action", "connect");
		json.put("userName", Resource.USERNAME);
		json.put("password", DigestUtils.md5Hex(Resource.PASSWORD));
		
		sendToServer(json);
	}
	
	public void register()
	{
		JSONObject json = new JSONObject();
		json.put("action", "register");
		json.put("userName", Resource.USERNAME);
		json.put("password", DigestUtils.md5Hex(Resource.PASSWORD));
		
		sendToServer(json);
	}

	public void sendToServer(JSONObject json)
	{
		try
		{
			final SSLSocket clientSocket = (SSLSocket)SSLSocketFactory.getDefault().createSocket(Resource.IP, Integer.parseInt(Resource.PORT));
			clientSocket.setEnabledCipherSuites(clientSocket.getSupportedCipherSuites());
			BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
			final BufferedReader bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			bWriter.write(json.toJSONString() + "\n");
			bWriter.flush();
			
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
						
						if (incomingMessage == null || incomingMessage.equals(""))
							continue;
						
						JSONObject incomingJSON = null;
						try
						{
							incomingJSON = (JSONObject)(new JSONParser().parse(incomingMessage));
						}
						catch(ParseException pe) { pe.printStackTrace(); }
						
						if(incomingJSON.get("action").equals("connected"))
						{
							JFrame go = new UserListGraphicalUserInterface(clientSocket);
			                go.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			                go.setSize(300, 600);
			                go.setResizable(true);
			                go.setVisible(true);
			                
			                setVisible(false);
			                break;
						}
						else if (incomingJSON.get("type") != null){// Registering
							JOptionPane.showMessageDialog(null, (String)incomingJSON.get("serverMessage"), "JIM", Integer.parseInt((String)incomingJSON.get("type")));
						}
					}
				}
			});
			t1.start();
		}
 		catch (Exception e) { e.printStackTrace(); }
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		Resource.USERNAME = name.getText();
		Resource.PASSWORD = new String(password.getPassword());
		
		if(e.getSource() == loginButton)
			connect();
		else if(e.getSource() == registerButton)
		{				
			if(Resource.USERNAME.equals(""))
				JOptionPane.showMessageDialog(null, "Username cannot be blank!", "JIM", JOptionPane.ERROR_MESSAGE);
			else if(Resource.PASSWORD.equals(""))
				JOptionPane.showMessageDialog(null, "Password cannot be blank!", "JIM", JOptionPane.ERROR_MESSAGE);
			else
				register();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		Resource.USERNAME = name.getText();
		Resource.PASSWORD = new String(password.getPassword());
		
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
			connect();
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
}