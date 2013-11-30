import javax.swing.JFrame;

public class Main
{
	public static JFrame go;
	
	public static void main(String[] args)
	{
		ConnectionGraphicalUserInterface cGUI = new ConnectionGraphicalUserInterface();
		
		cGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cGUI.setSize(300, 215);
		cGUI.setResizable(false);
		cGUI.setVisible(true);
	}
}