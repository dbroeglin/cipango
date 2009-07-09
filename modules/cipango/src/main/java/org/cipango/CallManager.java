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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import org.cipango.log.CallLog;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.log.Log;

public class CallManager extends AbstractLifeCycle  
{   
    protected Map<String, Call> _calls = new HashMap<String, Call>(1024);
    
    protected PriorityQueue<Call> _queue;
    private Thread _scheduler;
    
    private Server _server;
	
    //stats 
    private long _statsStartedAt = -1;
    private int _maxCalls;
    
    private CallLog _callLog;
	
    public CallManager() 
    { 
    	 _queue = new PriorityQueue<Call>(1024, new Comparator<Call>()
    			 {
					public int compare(Call call1, Call call2) 
					{
						if (call1 == call2)
							return 0;
						return (call1.nextExecutionTime() > call2.nextExecutionTime() ? 1 : -1);
					}
    			 });
    }
    
    public void doStart() throws Exception
    {
    	if (_callLog != null)
    	{
    		try
    		{
    			_callLog.start();
    		}
    		catch (Exception e)
    		{
    			Log.warn(e);
    		}
    	}
        new Thread(new Scheduler()).start();
        super.doStart();
    }
    
    public void doStop() throws Exception
    {
    	super.doStop();
    	
    	if (_scheduler != null)
    		_scheduler.interrupt();
    	
    	synchronized (_calls) {	_calls.clear(); } 
    }
    
    public void setCallLog(CallLog callLog)
    {
    	try
    	{
    		if (_callLog != null)
    			_callLog.stop();
    	}
    	catch (Exception e)
    	{
    		Log.warn(e);
    	}
    	if (getServer() != null)
    		getServer().getContainer().update(this, _callLog, callLog, "calllog", true);
    	
    	_callLog = callLog;
    	
    	try
    	{
    		if (isStarted() && (_callLog != null))
    			_callLog.start();
    	}
    	catch (Exception e)
    	{
    		throw new RuntimeException(e);
    	}
    }
    
    private Call newCall(String id)
    {
    	return new Call(id);
    }
    
    public Call lock(String callId)
    {
    	Call call = null;
    	
    	synchronized (_calls)
    	{
    		call = (Call) _calls.get(callId);
    		if (call == null)
    		{
    			call = newCall(callId);
    			call.setServer(_server);
    			
    			if (_callLog != null)
    	    		call.setLogger(_callLog.getLogger(callId));

    			_calls.put(call.getCallId(), call);
    			
    			if (_statsStartedAt > 0 && (_calls.size() > _maxCalls))
    				_maxCalls = _calls.size();
    		}
    	}
    	return call.lock();
    }
    
    public Call lock(Call call)
    {
    	return call.lock();
    }
    
    public void unlock(Call call)
    {
    	int holds = call.getHoldCount();
    	
    	if (holds == 1)
    	{
    		long time = call.nextExecutionTime();

        	if (time > 0)
        	{
        		while (time < System.currentTimeMillis())
        		{
        			call.runTimers();
        			time = call.nextExecutionTime();
        		}
        		if (call.isLogEnabled())
        			call.log("scheduling call in {} ms", (time - System.currentTimeMillis()));
        		
        		if (time > 0)
        		{
        			synchronized (_queue)
        			{
        				_queue.remove(call);
        				_queue.offer(call);
        				_queue.notifyAll();
        			}
        		}
        	}
        	
        	if (call.isDone())
        	{
        		synchronized (_calls)
            	{
            		_calls.remove(call.getCallId());
            	}
        	}
    	}
    	call.unlock();
    }
   
    private void runTimers(Call call)
	{
		call.lock();
		try
		{
			call.runTimers();
		}
		finally
		{
			unlock(call);
		}
	}
    
    public int getNbCalls()
    {
        return _calls.size();
    }
    
    public int getMaxCalls()
    {
        return _maxCalls;
    }
    
    public void statsReset() 
    {
        _statsStartedAt = _statsStartedAt == -1 ? -1 : System.currentTimeMillis();
        _maxCalls = _calls.size();
    }
    
    public void setStatsOn(boolean on) 
    {
        if (on && _statsStartedAt != -1) 
            return;
        
        statsReset();
        _statsStartedAt = on ? System.currentTimeMillis() : -1;
    }
    
	public boolean isStatsOn() 
	{
		return _statsStartedAt != -1;
	}
	
	public void setServer(Server server)
	{
		_server = server;
	}
	
	public Server getServer()
	{
		return _server;
	}
    
    class Scheduler implements Runnable
    {
    	public void run()
    	{
    		_scheduler = Thread.currentThread();
    		String name = _scheduler.getName();
    		_scheduler.setName("call-scheduler");
    		int priority = _scheduler.getPriority();
    		
    		try
    		{
    			_scheduler.setPriority(priority); // TODO
    			do
    			{
    				try
    				{
    					Call call;
    					long timeout;
    					
						synchronized (_queue)
						{
							//System.out.println(_queue);
							call = _queue.peek();
							timeout = (call != null ? call.nextExecutionTime() - System.currentTimeMillis() : Long.MAX_VALUE);
							
							if (timeout > 0)
							{
								//System.out.println("Waiting for call: " + (call == null ? "null" : call));
								_queue.wait(timeout);
							}
							else
							{
								_queue.poll();
							}
						}
						if (timeout <= 0)
						{
							//System.out.println("Running timers for call: " + (call == null ? "null" : call));
							runTimers(call);
						}
    				}
    				catch (InterruptedException e) { continue; }
    				catch (Throwable t) { Log.warn(t); }
    			}
    			while (isStarted());
    		}
    		finally
    		{
    			_scheduler.setName(name);
    			_scheduler.setPriority(priority);
    			_scheduler = null;
    			
    			String exit = "Call scheduler exited";
    			if (isStarted())
    				Log.warn(exit);
    			else
    				Log.debug(exit);
    		}
    	}
    }
}
