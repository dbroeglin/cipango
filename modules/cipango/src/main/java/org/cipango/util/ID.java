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

import org.cipango.Via;

public abstract class ID 
{
	private static long __id = 1;
    private static Random __random = new Random();
    
    private static String __localhost;
    
    private static String CALL_ID_PREFIX = "[[eid:";

    static 
    {
    	try 
    	{
    		__localhost = InetAddress.getLocalHost().getHostName();
    	} 
    	catch (Exception _) 
    	{
    		__localhost = "localhost";
    	}
    }
    
    public static final int MAX_CSEQ = (1 << 16);
    
    public static long newMessageID() 
    {
    	return __id++;
    }
    
    public static int newCSeq()
    {
        synchronized (__random)
        {
            return (int) (__random.nextDouble() * MAX_CSEQ);
        }
    }
    
    public static String newID(int length) 
    {
    	byte[] b = new byte[length];
    	synchronized (__random) 
    	{
        	__random.nextBytes(b);
		}
    	return HexString.toHexString(b);
    }

    public static String newTag() 
    {
    	return newID(4); // RFC 3261: at least 32 bits of randomness
    }
    
    public static String newSessionID()
    {
    	return newID(4);
    }
    
    public static String newCNonce()
    {
    	return newID(4);
    }
    
    public static String newBranch() 
    {
    	return Via.MAGIC_COOKIE + newID(6);
    }
    
    public static String newCallID() 
    {
    	return newID(4) + '@' + __localhost;
    }
    
    public static String newCallId(String callId)
    {
        StringBuffer sb = new StringBuffer(callId.length());
        if (callId.startsWith(CALL_ID_PREFIX))
        	callId = getCallId(callId);
        sb.append(CALL_ID_PREFIX);
        for (int i = 0; i < callId.length(); i++)
        {
            char c = callId.charAt(i);
            if (c == '@')
            {
                sb.append('%');
            }
            else if (c == '%')
            {
                sb.append("%%");
            }
            else 
            {
                sb.append(c);
            }
        }
        sb.append("]]").append(newID(4) + '@' + __localhost);
        return sb.toString();
    }
    
    public static String getCallId(String callId)
    {
        if (callId.charAt(0) == '[')
        {
        	int begin = 0;
        	while (callId.startsWith(CALL_ID_PREFIX, begin))
        	{
        		begin += 6;
        	}
            if (begin != 0)
            {
                int end = callId.indexOf("]]", begin);
                if (end != -1)
                {
                    String s = callId.substring(begin, end);
                    StringBuffer sb = new StringBuffer(s.length());
                    
                    boolean percent = false;
                    for (int i = 0; i < s.length(); i++)
                    {
                        char c = s.charAt(i);
                        if (c == '%')
                        {
                            if (percent)
                                sb.append(c);
                            percent = !percent;
                        }
                        else 
                        {
                            if (percent)
                            {
                                sb.append('@');
                                percent = false;
                            }
                            sb.append(c);
                        }
                    }
                    return sb.toString();
                }
            }
        }
        return callId;
    }
}
