package org.cipango.kaleo.location;

import javax.servlet.sip.URI;

public class Binding 
{
	private String _callId;
	private int _cseq;
	private URI _contact;
	private int _expires;
	private String _aor;
	
	public Binding(String aor, URI contact)
	{
		_aor = aor;
		_contact = contact;
	}
	
	public String getAOR()
	{
		return _aor;
	}
	
	public URI getContact()
	{
		return _contact;
	}
	
	public int getExpires()
	{
		return _expires;
	}
	
	public String getCallId()
	{
		return _callId;
	}
	
	public int getCSeq()
	{
		return _cseq;
	}
}
