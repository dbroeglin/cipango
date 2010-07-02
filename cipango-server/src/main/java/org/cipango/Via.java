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

package org.cipango;

import java.io.Serializable;
import java.util.*;

import javax.servlet.sip.ServletParseException;

import org.cipango.SipHeaders.HeaderInfo;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

public class Via implements Serializable, Cloneable
{	
    public static final String MAGIC_COOKIE = "z9hG4bK";
    
    private static final String BRANCH_PARAM 	= "branch";
    private static final String MADDR_PARAM 	= "maddr";
    private static final String RECEIVED_PARAM 	= "received";
    private static final String RPORT_PARAM 	= "rport";
    
    private String _via;
    private String _protocol;
    private String _transport;
    private String _host;
    private int _port = -1;
    
    private HashMap _params = new HashMap();
    
    public Via(String via) throws ServletParseException 
    {
    	_via = via;
    	parse();
    }
    
    public int getType()
    {
    	return HeaderInfo.VIA;
    }

    private void parse() throws ServletParseException 
    {	
        int indexSp = _via.indexOf(' ');
        String s = _via.substring(0, indexSp);
        
        int indexTransport = s.lastIndexOf('/');
        _protocol = s.substring(0, indexTransport);
        _transport = s.substring(indexTransport + 1);
        
        while (SipGrammar.isLWS(_via.charAt(indexSp))) 
        { 
        	indexSp++;
        }

        int indexPort = -1;
        if (_via.charAt(indexSp) == '[')
        {
        	int i = _via.indexOf(']', indexSp);
        	if (i < 0)
        		throw new ServletParseException("Invalid IPv6 in " + _via);
        	indexPort = _via.indexOf(':', i);
        }
        else 
        {
        	 indexPort = _via.indexOf(':', indexSp);
        }
        
        int indexParams = _via.indexOf(';', indexSp);
        if (indexPort > -1 && (indexPort < indexParams || indexParams < 0)) 
        {
            _host = _via.substring(indexSp, indexPort);
            String sPort;
            if (indexParams < 0)
                sPort = _via.substring(indexPort + 1);
            else 
                sPort = _via.substring(indexPort + 1, indexParams).trim();
            try 
            {
            	_port = Integer.parseInt(sPort);
            } 
            catch (NumberFormatException _) 
            {
            	throw new ServletParseException("Invalid port [" + sPort + "] in [" + _via + "]");
            }
        } 
        else 
        {
            _port = -1;
            if (indexParams < 0) 
                _host = _via.substring(indexSp);
            else
                _host = _via.substring(indexSp, indexParams).trim();
        }

        if (indexParams > 0) 
            parseParams(_via.substring(indexParams + 1));
    }
    
	private void parseParams(String sParams) throws ServletParseException 
	{
		StringTokenizer st = new StringTokenizer(sParams, ";");
		while (st.hasMoreTokens()) 
		{
			String param = st.nextToken();
			String name;
			String value;
			int index = param.indexOf('=');
			
			if (index < 0) 
			{
				name  = param.trim();
				value = "";
			} 
			else 
			{
				name  = param.substring(0, index).trim();
				value = param.substring(index + 1).trim();
			}
			if (!SipGrammar.__param.containsAll(name)) 
			{
				throw new ServletParseException("Invalid parameter name [" 
						+ name + "] in [" + _via + "]");
			}
			if (!SipGrammar.__param.containsAll(value) && !SipGrammar.isToken(value)) 
			{
				throw new ServletParseException("Invalid parameter value [" 
						+ value + "] in [" + _via + "]");
			}			
			_params.put(name.toLowerCase(), value);
		}
	}
	
    public Via(String protocol, String transport, String host) 
    {
        this(protocol, transport, host, -1);
    }

    public Via(String protocol, String transport, String host, int port) 
    {
        _protocol = protocol;
        _transport = transport;
        _host = host;
        _port = port;
    }

    public String getProtocol() 
    {
        return _protocol;
    }

    public String getTransport() 
    {
        return _transport;
    }

    public void setTransport(String transport) 
    {
    	_transport = transport;
    }
    
    public String getHost() 
    {
        return _host;
    }
    
    public void setHost(String host) 
    {
    	_host = host;
    }

    public int getPort() 
    {
        return _port;
    }
    
    public void setPort(int port) 
    {
    	_port = port;
    }

    public String getBranch() 
    {
        return (String) getParameter(BRANCH_PARAM);
    }
    
    public void setBranch(String branch) 
    {
        addParameter(BRANCH_PARAM, branch);
    }

    public String getMAddr() 
    {
        return (String) getParameter(MADDR_PARAM);
    }

    public String getReceived() 
    {
        return (String) getParameter(RECEIVED_PARAM);
    }
    
    public void setReceived(String received) 
    {
    	addParameter(RECEIVED_PARAM, received);
    }
    
    public String getRport() 
    {
    	return (String) getParameter(RPORT_PARAM);
    }
    
    public void setRport(String rport) 
    {
    	addParameter(RPORT_PARAM, rport);
    }
    
    public void addParameter(String name, String value) 
    {
        _params.put(name, value);
    }

    public void addParameter(String name) 
    {
        addParameter(name, "");
    }

    public String getParameter(String name) 
    {
        return (String) _params.get(name);
    }

    public Object clone() 
    {
        try 
        {
            Via clone = (Via) super.clone();
            if (_params != null)
                clone._params = (HashMap) _params.clone();
            
            return clone;
        } 
        catch (CloneNotSupportedException _) 
        {
            throw new RuntimeException("!cloneable: " + this);
        }
    }

    public String toString() 
    {
        StringBuffer sb = new StringBuffer();
        sb.append(_protocol);
        sb.append('/');
        sb.append(_transport);
        sb.append(' ');
        sb.append(_host);
        if (_port > -1) 
        {
            sb.append(':'); 
            sb.append(_port);
        }
        
        Iterator iter = _params.keySet().iterator();
        while (iter.hasNext()) 
        {
            String name = (String) iter.next();
            String value = getParameter(name);
            sb.append(';');
            sb.append(name);
            if (value != null && value.length() > 0) 
            {
                sb.append('=');
                sb.append(value);
            }
        }

        return sb.toString();
    }
    
	public Buffer toBuffer() 
	{
		return new ByteArrayBuffer(toString());
	}
}
