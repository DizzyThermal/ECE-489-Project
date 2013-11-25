public class User
{
	private int id;
	private String name;
	private String ip;
	
	public User(int id, String name, String ip)
	{
		this.id = id;
		this.name = name;
		this.ip = ip;
	}
	
	public int getId()					{ return id;		}
	public String getName()				{ return name;		}
	public String getIp()				{ return ip;		}
	
	public void setId(int id)			{ this.id = id;		}
	public void setName(String name)	{ this.name = name;	}
	public void setIp(String ip)		{ this.ip = ip;		}
	
	public void setInfo(int id, String name)
	{
		this.id = id;
		this.name = name;
	}
	
	public void setInfo(int id, String name, String ip)
	{
		this.id = id;
		this.name = name;
		this.ip = ip;
	}
}