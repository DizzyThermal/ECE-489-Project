public class User
{
	private int id;
	private String name;
	private String ip;
	private int port;
	
	public User(int id, String name, String ip, int port)
	{
		this.id = id;
		this.name = name;
		this.ip = ip;
		this.port = port;
	}
	
	public int getId()					{ return id;		}
	public String getName()				{ return name;		}
	public String getIp()				{ return ip;		}
	public int getPort()				{ return port;		}
	
	public void setId(int id)			{ this.id = id;		}
	public void setName(String name)	{ this.name = name;	}
	public void setIp(String ip)		{ this.ip = ip;		}
	public void setPort(int port)	{ this.port = port;	}
}