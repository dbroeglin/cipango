// ========================================================================
// Copyright 2008-2009 NEXCOM Systems
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


package org.cipango.sip;

public abstract class SipConnectors 
{
	private SipConnectors() { }
	
	public static final String UDP = "UDP";
	public static final String TCP = "TCP";
	public static final String TLS = "TLS";
	
	public static final int UDP_ORDINAL = 1;
	public static final int TCP_ORDINAL = 2;
	public static final int TLS_ORDINAL = 3;
	
	public static String getName(int ordinal) 
	{
		switch (ordinal) 
		{
		case UDP_ORDINAL:
			return UDP;
		case TCP_ORDINAL:
			return TCP;
		case TLS_ORDINAL:
			return TLS;
		default:
			return null;
		}
	}
	
	public static int getOrdinal(String name) 
	{
		if (UDP.equalsIgnoreCase(name)) 
			return UDP_ORDINAL;
		else if (TCP.equalsIgnoreCase(name)) 
			return TCP_ORDINAL;
		else if (TLS.equalsIgnoreCase(name))
			return TLS_ORDINAL;
		else 
			return -1;
	}
	
	public static boolean isReliable(int ordinal) 
	{
		switch (ordinal) 
		{
		case UDP_ORDINAL:
			return UdpConnector.RELIABLE;
		case TCP_ORDINAL:
			return TcpConnector.RELIABLE;
		case TLS_ORDINAL:
			return true;
		default:
			throw new IllegalArgumentException("Unknown connector: " + ordinal);
		}
	}
	
	public static int getDefaultPort(int ordinal)
	{
		switch (ordinal)
		{
		case UDP_ORDINAL:
			return UdpConnector.DEFAULT_PORT;
		case TCP_ORDINAL:
			return TcpConnector.DEFAULT_PORT;
		case TLS_ORDINAL:
			return 5061;
		default:
			throw new IllegalArgumentException("Unknown connector: " + ordinal);
		}
	}
}
