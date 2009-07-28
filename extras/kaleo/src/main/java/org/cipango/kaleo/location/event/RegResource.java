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

package org.cipango.kaleo.location.event;

import org.cipango.kaleo.event.AbstractEventResource;
import org.cipango.kaleo.event.State;

public class RegResource extends AbstractEventResource
{
	private ReginfoDocument _content;
	
	public RegResource(String uri)
	{
		super(uri);
	}

	public State getState() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isDone() 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public long nextTimeout() 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public void doTimeout(long time) 
	{
		// TODO Auto-generated method stub	
	}
}
