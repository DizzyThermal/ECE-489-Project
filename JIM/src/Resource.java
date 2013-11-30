public class Resource
{
	public static final String VERSION_NUMBER		= "v0.0.2";
	public static final String VERSION_CODENAME		= "Banana";
	
	public static String IP							= "localhost";
	public static String PORT						= "4444";
	public static String LISTENING_PORT				= "55555";
	
	public static String USERNAME					= getUsername();
	public static String PASSWORD					= "";
	
	public static String getUsername()
	{
		return System.getProperty("user.name");
	}
}