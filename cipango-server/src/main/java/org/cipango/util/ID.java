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

package org.cipango.util;

import java.net.InetAddress;
import java.util.Random;

import org.cipango.sip.Via;

/**
 * Groups all SIP ids generation and parsing (call, session, transaction ...)
 */
public abstract class ID 
{
	public static final String APP_SESSION_ID_PARAMETER = "app-session-id";
	
	public static final String CONTEXT_ATTRIBUTE = "context";
	public static final String SESSION_KEY_ATTRIBUTE = "session.key";

	private static String __localhost;
	private static Random __random = new Random();
    public static final int MAX_CSEQ = (1 << 16);
	private static String __magicCookie = "aK5q9iC";
    
	static
	{
		try
		{
			__localhost = InetAddress.getLocalHost().getHostName();
		}
		catch (Exception e)
		{
			__localhost = "localhost";
		}
	}
	
	/** SipApplicationSession id */
    public static String newAppSessionId()
    {
    	return newID(4);
    }
    
    /** SipSession id */
    public static String newSessionId()
    {
    	return newID(4);
    }
    
    /** Call id */
    public static String newCallId()
    {
    	return newID(4) + '@' + __localhost;
    }
    
    /** dialog tags (from/to) */
    public static String newTag() 
    {
    	return newID(4); // RFC 3261: at least 32 bits of randomness
    }
    
    /** Digest cnonce */
    public static String newCNonce()
    {
    	return newID(4);
    }
    
    /** transaction branch */ 
    public static String newBranch() 
    {
    	return Via.MAGIC_COOKIE + newID(6);
    }
    
    /** cseq */
    public static int newCSeq()
    {
        synchronized (__random)
        {
            return (int) (__random.nextDouble() * MAX_CSEQ);
        }
    }
    
    /** 
   	 * Random ID generator
   	 * 
     * @param length	the number of bytes of randomness
     * @return	a new random ID as an hex-string  
     */
    public static String newID(int length) 
    {
    	byte[] b = new byte[length];
    	synchronized (__random) 
    	{
        	__random.nextBytes(b);
		}
    	return HexString.toHexString(b);
    }
    	
	public static String newCallId(String callId)
    {
    	if (callId.startsWith(__magicCookie))
    	{
    		int i = callId.indexOf('*');
    		
    		StringBuilder sb = new StringBuilder(callId.length());
    		sb.append(__magicCookie);
    		sb.append('-');
    		sb.append(ID.newID(3));
    		sb.append('*');
    		sb.append(callId.substring(i+1));
    		return sb.toString();
    	}
    	else
    	{
    		StringBuilder sb = new StringBuilder(callId.length() + __localhost.length() + 14);
    		sb.append(__magicCookie);
    		sb.append('-');
    		sb.append(ID.newID(3));
    		sb.append('*');
    		for (int i = 0; i < callId.length(); i++)
    		{
    			char c = callId.charAt(i);
    			if (c == '@')
    				sb.append('%');
    			else if (c == '%')
    				sb.append("%%");
    			else
    				sb.append(c);
    		}
    		sb.append('@');
    		sb.append(__localhost);
    		return sb.toString();
    	}		
    }
	
	public static String getCallSessionId(String callId)
	{
	   	if (!callId.startsWith(__magicCookie))
	   		return callId;
	   	
	   	int i = callId.indexOf('*', __magicCookie.length() + 7);
	   	int j = callId.indexOf('@', i+1);
	   	
	   	StringBuilder sb = null;
	   	boolean escape = false;
	   	
	   	for (int k = i+1; k < j; k++)
	   	{
	   		char c = callId.charAt(k);
	   		if (c == '%')
	   		{
	   			if (sb == null)
	   			{
	   				sb = new StringBuilder(j-i);
	   				sb.append(callId.substring(i+1, k));
	   			}
	   			if (escape)
	   				sb.append('%');
	   			escape = ! escape;
	   		}
	   		else
	   		{
	   			if (escape)
	   			{
	   				sb.append('@');
	   				escape = false;
	   			}
	   			if (sb != null)
	   				sb.append(c);
	   		}
	   	}
	   	if (sb != null)
	   		return sb.toString();
	   	return callId.substring(i+1, j);
	}	
	
	public static String getIdFromKey(String applicationName, String sessionKey)
	{
		return "skey-" + applicationName + "-" + sessionKey;
	}
	
	public static boolean isKey(String id)
	{
		return id.startsWith("skey-");
	}
   
	public static void main(String[] args) throws Exception 
    {/*
		MessageDigest md = MessageDigest.getInstance("MD5");
		System.out.println(HexString.toHexString(new byte[] {'1', '.', '1'}));
			
		System.out.println(ID.newID(4));
		
		System.out.println("1.session-key".hashCode());*/
		long ts = (System.currentTimeMillis() / 1000) * 5;
		long hi = ts & 0xffffffffL;
		long id = hi << 32;
		System.out.println(ts);
		System.out.println(hi);
		System.out.println(Long.toHexString(ts));
		System.out.println(Long.toHexString(hi));
		
	}
}