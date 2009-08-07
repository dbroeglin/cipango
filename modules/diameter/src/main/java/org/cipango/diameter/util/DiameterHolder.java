// ========================================================================
// Copyright 2009 NEXCOM Systems
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
package org.cipango.diameter.util;

import java.io.IOException;

import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.app.DiameterListener;

public class DiameterHolder implements DiameterListener
{
	private DiameterListener _diameterListener;
	private ClassLoader _classLoader;
	
	public DiameterHolder(DiameterListener diameterListener, ClassLoader classLoader)
	{
		_diameterListener = diameterListener;
		_classLoader = classLoader;
	}
	
	public void handle(DiameterMessage message) throws IOException
	{
		ClassLoader oldClassLoader = null;
		Thread currentThread = null;
		
		if (_classLoader != null)
		{
			currentThread = Thread.currentThread();
			oldClassLoader = currentThread.getContextClassLoader();
			currentThread.setContextClassLoader(_classLoader);
		}
		try
		{
			_diameterListener.handle(message);
		}
		finally
		{
			if (_classLoader != null)
				currentThread.setContextClassLoader(oldClassLoader);
		}
	}


	
}
