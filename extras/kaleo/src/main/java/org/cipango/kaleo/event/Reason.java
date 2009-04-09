package org.cipango.kaleo.event;

public enum Reason 
{
	DEACTIVATED("deactivated"), 
	PROBATION("probation"), 
	REJECTED("rejected"),
	TIMEOUT("timeout"), 
	GIVEUP("giveup"), 
	NORESOURCE("noresource");
	
	private String _name;

	private Reason(String name)
	{
		_name = name;
	}

	public String getName()
	{
		return _name;
	}
}
