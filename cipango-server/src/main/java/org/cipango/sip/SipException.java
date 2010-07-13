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

import java.io.IOException;

public class SipException extends IOException 
{
	private int _code;
	
	public SipException(int code) 
    {
		_code = code;
	}
    
    public SipException(int code, String message)
    {
        super(message);
        _code = code;
    }
    
    public SipException(int code, Throwable t)
    {
    	super(t.getMessage());
        _code = code;
        initCause(t);
    }
    
    public int getCode()
    {
        return _code;
    }
}
