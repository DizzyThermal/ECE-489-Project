import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
	
	ChatWindowGraphicalUserInterface(int id, String username, String ip, int port)
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
	}
	
	/*
	ChatWindowGraphicalUserInterface(int id, String username, String ip, String message)
	{
		super(username);
		setLayout(new BorderLayout());
		
		this.id = id;
		this.username = username;
		this.ip = ip;
		
		createPanel();
		messageArea.setEditable(false);
		
		// Create Socket Connection
	}
	*/
	
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