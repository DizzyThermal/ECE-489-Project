import javax.swing.JFrame;

public class Main
{
	public static JFrame go;
	public static boolean connectionGUIStatus = false;
	public static boolean registering = false;
	
	public static void main(String[] args)
	{
		ConnectionGraphicalUserInterface cGUI = new ConnectionGraphicalUserInterface();
		
		cGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cGUI.setSize(300, 350);
		cGUI.setResizable(false);
		cGUI.setVisible(true);

		while(!connectionGUIStatus)
		{
			try { Thread.sleep(1);}
			catch (InterruptedException e1) { e1.printStackTrace(); }
		}
		
		go = new UserListGraphicalUserInterface(registering);
		go.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		go.setSize(300, 600);
		go.setResizable(true);
		go.setVisible(true);
	}
}