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
	public Thread thread;
	
	
	ChatWindowGraphicalUserInterface(int _id, String _username, String _ip, int _port, final String side)
	{
		setLayout(new BorderLayout());
		System.out.println("ChatWindowGraphicalUserInterface Entry Point");
		id = _id;
		username = _username;
		ip = _ip;
		port = _port;

		System.out.println("Calling create panel");
		createPanel();
		messageArea.setEditable(false);

		System.out.println("Creating socket");
		// Create Socket Connection
		thread = new Thread(){
			@Override
			public void run(){
				if(side.equals(Resource.SERVER)){
					// Create the server side of the connection
					try
					{
						System.out.println("Creating Server Side "+ip+" "+port);
						System.out.println("SSL Server Setup");
						serverSocket = (SSLServerSocket) SSLServerSocketFactory.getDefault().createServerSocket(port);
						System.out.println("SSL Server wait for accept");
						connectedServerSocket = (SSLSocket)serverSocket.accept();
						System.out.println("Cipher Suites");
						serverSocket.setEnabledCipherSuites(connectedServerSocket.getSupportedCipherSuites());
						System.out.println("SSL Server Streams");
						bWriter = new BufferedWriter(new OutputStreamWriter(connectedServerSocket.getOutputStream()));
						bReader = new BufferedReader(new InputStreamReader(connectedServerSocket.getInputStream()));
					}
					catch (Exception e) { e.printStackTrace(); }
		
					System.out.println("Listening on Port: " + port);
				}
				else{
					// Create client side of connection
					try {
						sleep(1000);
						System.out.println("Creating Client Side "+ip+" "+port);
						clientSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(ip, port);
						System.out.println("Setting Cipher Suites");
						clientSocket.setEnabledCipherSuites(clientSocket.getSupportedCipherSuites());
						System.out.println("SSL client streams");
						bWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
						bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					} catch (Exception e) { e.printStackTrace(); }
					
				}
			}
		};
		thread.start();
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