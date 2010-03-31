// ========================================================================
// Copyright 2010 NEXCOM Systems
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.cipango.snmp;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.smi.OID;

public class SnmpAddress
{
	private int _port;
	private String _host;
	private String _transport = "UDP";
	private InetAddress _inetAddress;
	
	public int getPort()
	{
		return _port;
	}
	public void setPort(int port)
	{
		_port = port;
	}
	public String getHost()
	{
		return _host;
	}
	public void setHost(String host)
	{
		_host = host;
	}
	public String getTransport()
	{
		return _transport;
	}
	public void setTransport(String transport)
	{
		_transport = transport;
		if (!isTcp() && !isUdp())
			throw new IllegalArgumentException("Invalid transport: " + transport);
	}
	public boolean isTcp()
	{
		return _transport.equalsIgnoreCase("tcp");
	}
	public boolean isUdp()
	{
		return _transport.equalsIgnoreCase("udp");
	}
	
	public OID getTransportDomain()
	{
		if (isUdp())
		{
			try
			{
				if (getInetAddress() instanceof Inet4Address)
					return TransportDomains.transportDomainUdpIpv4;
				else
					return TransportDomains.transportDomainUdpIpv6;
			}
			catch (UnknownHostException e)
			{
				return TransportDomains.transportDomainUdpIpv4;
			}
		}
		else
		{
			try
			{
				if (getInetAddress() instanceof Inet4Address)
					return TransportDomains.transportDomainTcpIpv4;
				else
					return  TransportDomains.transportDomainTcpIpv6;
			}
			catch (UnknownHostException e)
			{
				return TransportDomains.transportDomainTcpIpv4;
			}
		}
	}
	
	public InetAddress getInetAddress() throws UnknownHostException
	{
		if (_inetAddress == null)
			_inetAddress = InetAddress.getByName(_host);
		return _inetAddress;
	}
	
	public void setInetAddress(InetAddress inetAddress)
	{
		_inetAddress = inetAddress;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof SnmpAddress))
			return false;
		return toString().equals(o.toString());
	}
	
	@Override
	public String toString()
	{
		return _transport + "/" + getHost() + ":" + getPort();
	}
	
}
