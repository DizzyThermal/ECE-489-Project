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
	
	public SSLSocket chatSocket;
	public BufferedWriter bWriter;
	public BufferedReader bReader;
	
	public Thread thread;
	public String username;

	ChatWindowGraphicalUserInterface(final SSLSocket chatSocket, final String username)
	{
		this.chatSocket = chatSocket;
		this.chatSocket.setEnabledCipherSuites(chatSocket.getSupportedCipherSuites());
		this.username = username;
		
		try
		{
			bWriter = new BufferedWriter(new OutputStreamWriter(chatSocket.getOutputStream()));
			bReader = new BufferedReader(new InputStreamReader(chatSocket.getInputStream()));
		}
		catch(IOException ioe) { ioe.printStackTrace(); }
		
		initGUI();
		
		
		thread = (new Thread()
		{
			@Override
			public void run()
			{
				while(!chatSocket.isClosed())
				{
					String incomingMessage = null;
					try
					{
						incomingMessage = bReader.readLine();
					}
					catch(IOException ioe) { ioe.printStackTrace(); }
					if((incomingMessage != null) && !incomingMessage.equals("") && !incomingMessage.equals("[]"))
						continue;
					
					// We Got Something!
					JSONObject incomingJSON = null;
					try
					{
						incomingJSON = (JSONObject)(new JSONParser().parse(incomingMessage));
					}
					catch (ParseException e) { e.printStackTrace(); }
					append("\n" + username + ": " + incomingJSON.get("message"));
				}
			}
		});
		thread.start();
	}
	
	public void initGUI()
	{
		setResizable(false);
		setSize(600,400);
		setLayout(new FlowLayout());
		
		messageScrollPane.setPreferredSize(new Dimension(550, 325));
		messageArea.setEditable(false);
		
		messageField.setPreferredSize(new Dimension(550, 25));
		
		add(messageScrollPane);
		add(messageField);
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
				
				JSONObject json = new JSONObject();
				json.put("message", messageField.getText());
				
				try
				{
					bWriter.write(json.toJSONString() + "\n");
					bWriter.flush();
				}
				catch (IOException e1) { e1.printStackTrace(); }
			}
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}
}