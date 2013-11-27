import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.UnknownHostException;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JViewport;

public class ChatWindowGraphicalUserInterface extends JFrame implements KeyListener
{
	public JTextArea messageArea = new JTextArea();
	public JScrollPane messageScrollPane = new JScrollPane(messageArea);
	public JTextField messageField = new JTextField();
	
	public int id;
	public String username;
	public String ip;
	public int port;
	
	// Socket Attributes attributes
	public static SSLServerSocket serverSocket;
	public static SSLSocket connectedServerSocket;
	public static SSLSocket clientSocket;
	public BufferedWriter bWriter;
	public BufferedReader bReader;
	
	
	ChatWindowGraphicalUserInterface(int id, String username, String ip, int port, String side)
	{
		super(username);
		setLayout(new BorderLayout());
		
		this.id = id;
		this.username = username;
		this.ip = ip;
		this.port = port;
		
		createPanel();
		messageArea.setEditable(false);
		
		// Create Socket Connection
		if(side.equals(Resource.SERVER)){
			// Create the server side of the connection
			try
			{
				serverSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(this.port);
				connectedServerSocket = (SSLSocket)serverSocket.accept();
				serverSocket.setEnabledCipherSuites(connectedServerSocket.getSupportedCipherSuites());
				bWriter = new BufferedWriter(new OutputStreamWriter(connectedServerSocket.getOutputStream()));
				bReader = new BufferedReader(new InputStreamReader(connectedServerSocket.getInputStream()));
			}
			catch (Exception e) { e.printStackTrace(); }

			System.out.println("Listening on Port: " + this.port);
		}
		else{
			// Create client side of connection
			try {
				clientSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(ip, port);
				clientSocket.setEnabledCipherSuites(clientSocket.getSupportedCipherSuites());
				bWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (Exception e) { e.printStackTrace(); }
			
		}
	}
	
	
	public void createPanel()
	{
		// Create GUI - I'll do this (Steve)
	}
	
	public void append(String message)
	{
		messageArea.setText(messageArea.getText() + "\n" + username + ": " + message);
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if(!messageField.getText().equals("") && messageField.getText() != null)
			{
				messageArea.setText(messageArea.getText() + "\n" + Resource.USERNAME + ": " + messageField.getText());
				
				// Send to Client
			}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
	
	public int getId() { return id; }
	
}