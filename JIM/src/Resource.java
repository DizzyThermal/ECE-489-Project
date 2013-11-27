public class Resource
{
	public static final String VERSION_NUMBER		= "v0.0.1";
	public static final String VERSION_CODENAME		= "Apple";			// Versioning after Fruit
	
	public static String IP							= "diniz.dyndns-home.com";
	public static String PORT						= "4444";
	
	public static String USERNAME					= getUsername();
	public static String PASSWORD					= "";
	
	public static String getUsername()
	{
		return System.getProperty("user.name");
	}
}