import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.SSLSocket;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ChatWindowGraphicalUserInterface extends JFrame implements KeyListener
{
	public JTextArea messageArea = new JTextArea();
	public JScrollPane messageScrollPane = new JScrollPane(messageArea);
	public JTextField messageField = new JTextField();
	
	public Thread thread;
	public String username;
	public int id;
	
	ChatWindowGraphicalUserInterface(int id, String username, String initialMessage)
	{
		this.id = id;
		this.username = username;
		
		setTitle(username);
		initGUI(initialMessage);
	}
	
	public void initGUI(String initialMessage)
	{
		setResizable(false);
		setSize(600,400);
		setLayout(new FlowLayout());
		
		messageScrollPane.setPreferredSize(new Dimension(550, 325));
		messageArea.setEditable(false);
		
		messageField.setPreferredSize(new Dimension(550, 25));
		
		add(messageScrollPane);
		add(messageField);
		
		if(initialMessage != null)
			append(initialMessage);
		
		setVisible(true);
		messageField.addKeyListener(this);
	}
	
	public void append(String message)
	{
		messageArea.setText(messageArea.getText() + username + ": " + message + "\n");
	}
	
	@Override
	public void keyPressed(KeyEvent e)
	{
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			if(!messageField.getText().equals("") && messageField.getText() != null)
			{
				messageArea.setText(messageArea.getText() + Resource.USERNAME + ": " + messageField.getText() + "\n");
				
				JSONObject json = new JSONObject();
				json.put("action", "message");
				json.put("userId", id);
				json.put("userMessage", messageField.getText());
				
				UserListGraphicalUserInterface.sendMessageToServer(id, json.toJSONString());
				messageField.setText("");
			}
		}
	}
	
	public int getId() { return id; };
	
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
}