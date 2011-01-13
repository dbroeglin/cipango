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
package org.cipango.client;

import junit.framework.Assert;

public abstract class UaRunnable extends Thread
{
	protected SipSession _session;
	private Throwable _e;
	private Boolean _isDone = Boolean.FALSE;
	
	public UaRunnable(SipSession session)
	{
		_session = session;
	}
	
	public void run()
	{
		try
		{
			doTest();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			_e = e;
		}
		finally
		{
			_isDone = true;
			synchronized (_isDone)
			{
				_isDone.notify();
			}
		}
	}
	
	public abstract void doTest() throws Throwable;
	

/*	protected void handlePotentialCancel() throws ParseException
	{
		RequestEvent event =_call.getAllReceivedRequestEvents().get(_call.getAllReceivedRequestEvents().size() -2);
		if (event.getRequest().getMethod().equals("CANCEL"))
		{
			_call.sendResponse(event, Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
		}
	}*/

	public Throwable getException()
	{
		return _e;
	}

	public boolean isDone()
	{
		return _isDone;
	}

	public String getUserName()
	{
		return null;
	}
	
	public void assertDone() throws Throwable
	{
		if (_e != null)
			throw _e;
		if (_isDone)
			return;
		
		synchronized (_isDone)
		{
			try
			{
				_isDone.wait(2000);
			}
			catch (InterruptedException e)
			{
			}
		}
		if (_e != null)
			throw _e;
		if (!_isDone)
			Assert.fail(getUserName() + " not done");
	}
	
}
