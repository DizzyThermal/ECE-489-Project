import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ConnectionGraphicalUserInterface extends JFrame implements ActionListener, KeyListener
{
	JPanel mainPanel = new JPanel();

	JLabel titleLabel = new JLabel("Connection Information");
	JLabel nameLabel = new JLabel("Username: ");
	JLabel passwordLabel = new JLabel("Password: ");
	
	JButton loginButton = new JButton("Login");
	JButton registerButton = new JButton("Register");
	
	JTextField name = new JTextField();
	JPasswordField password = new JPasswordField();
	
	ConnectionGraphicalUserInterface()
	{
		super("Connection Information");
		FlowLayout fl = new FlowLayout();
		fl.setAlignment(FlowLayout.LEFT);
		setLayout(fl);
		
		createPanel();
		add(mainPanel);
	}
	
	public void createPanel()
	{
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
		password.setText(Resource.IP);
		password.addKeyListener(this);
		mainPanel.add(password);
		
		mainPanel.add(loginButton);
		mainPanel.addKeyListener(this);
		loginButton.addActionListener(this);
	}
	
	public void login()
	{
		Resource.USERNAME = name.getText();
		Resource.PASSWORD = new String(password.getPassword());
		this.setVisible(false);
		Main.connectionGUIStatus = true;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == loginButton)
		{
			login();
		}
		else if(e.getSource() == registerButton)
		{
			Main.registering = true;
			login();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
		{
			login();
		}
		else if(e.getSource() == registerButton)
		{
			Main.registering = true;
			login();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
}