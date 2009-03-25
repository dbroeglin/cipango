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

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.sip.SipServletMessage;

import org.mortbay.log.Log;

public class ExceptionUtil 
{
	public static void fillStackTrace(SipServletMessage message, Throwable t)
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(os);
        pw.print("Exception while handling request: ");
        if (t.getCause() != null)
            t.getCause().printStackTrace(pw);
        else
        	t.printStackTrace(pw);
        pw.flush();
        try 
        {
        	message.setContent(os.toByteArray(), "text/plain");
		} 
        catch (UnsupportedEncodingException e)
        {
			Log.ignore(e);
		} 
	}
}
