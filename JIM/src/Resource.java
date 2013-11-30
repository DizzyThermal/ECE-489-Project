public class Resource
{
	public static final String VERSION_NUMBER		= "v1.0.0";
	public static final String VERSION_CODENAME		= "Clementine";
	
	public static String IP							= "localhost";
	public static String PORT						= "4444";
	
	public static String USERNAME					= getUsername();
	public static String PASSWORD					= "";
	
	public static String getUsername()
	{
		return System.getProperty("user.name");
	}
}