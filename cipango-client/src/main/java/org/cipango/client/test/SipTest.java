package org.cipango.client.test;

import java.util.HashMap;
import java.util.Map;

import org.cipango.client.SipClient;
import org.eclipse.jetty.util.log.Log;

public class SipTest 
{
	private static final int DEFAULT_PORT = 5060;
	
	private static Map<Integer, SipClient> _clients = new HashMap<Integer, SipClient>();
	
	public static SipTester create(String user) throws Exception 
	{
		SipClient client = getOrCreate(DEFAULT_PORT);
		SipTester tester = new SipTester(client.createSipURI(user, client.getContact().getHost()));
		
		client.addAgent(tester);
		
		return tester;
	}
		
	protected static SipClient getOrCreate(int port) throws Exception
	{
		synchronized (_clients)
		{
			SipClient client = _clients.get(port);
			if (client == null)
			{
				client = new SipClient(port);
				_clients.put(port, client);
				client.start();
			}
			return client;
		}
	}
	
	public static void reset()
	{
		synchronized (_clients)
		{
			for (SipClient client : _clients.values())
			{
				try { client.stop(); } catch (Exception e ) { Log.ignore(e); }
			}
			_clients.clear();
		}
	}
}
