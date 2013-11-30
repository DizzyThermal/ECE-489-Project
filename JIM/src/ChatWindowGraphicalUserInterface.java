import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatWindowGraphicalUserInterface extends JFrame implements KeyListener, WindowListener
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
		
		this.addWindowListener(this);
		
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

				UserListGraphicalUserInterface.sendMessageToServer(id, messageField.getText());
				messageField.setText("");
			}
		}
	}
	
	public int getId() { return id; };
	
	@Override
	public void keyTyped(KeyEvent e) {}
	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e)
	{
		setVisible(false);
		messageArea.setText("");
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e)
	{
		messageField.requestFocus();
	}
}