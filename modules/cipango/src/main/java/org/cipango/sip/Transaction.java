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

import java.io.Serializable;

import org.cipango.Call;
import org.cipango.SipRequest;
import org.cipango.Server;
import org.cipango.Call.TimerTask;
import org.mortbay.log.Log;

public abstract class Transaction implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static final int STATE_UNDEFINED  = 0;
    public static final int STATE_CALLING    = 1;
    public static final int STATE_TRYING     = 2;
    public static final int STATE_PROCEEDING = 3;
    public static final int STATE_COMPLETED  = 4;
    public static final int STATE_CONFIRMED  = 5;
    public static final int STATE_TERMINATED = 6;
    
    public static final String[] STATES = 
    {
        "Undefined", 
        "Calling", 
        "Trying", 
        "Proceeding", 
        "Completed", 
        "Confirmed", 
        "Terminated"
    };
    
    protected static final int TIMER_A = 1;
    protected static final int TIMER_B = 2;
    protected static final int TIMER_D = 4;
    protected static final int TIMER_E = 5;
    protected static final int TIMER_F = 6;
    protected static final int TIMER_G = 7;
    protected static final int TIMER_H = 8;
    protected static final int TIMER_I = 9;
    protected static final int TIMER_J = 10;
    protected static final int TIMER_K = 11;
           
    public static final int DEFAULT_T1 = 500;
    public static final int DEFAULT_T2 = 4000;
    public static final int DEFAULT_T4 = 5000;
    public static final int DEFAULT_TD = 32000;
    
    public static int __T1 = DEFAULT_T1;
    public static int __T2 = DEFAULT_T2;
    public static int __T4 = DEFAULT_T4;
    public static int __TD = DEFAULT_TD;
    
    private TimerTask[] _timers = new TimerTask[] { null, null, null, null, null, null, null, null, null, null, null, null };
    
    protected int _state;
    private String _branch;
    private String _key;
    protected SipRequest _request;
    protected Call _call;
    protected boolean _cancel;
    
    public Transaction(SipRequest request, String branch)
    {
        _request = request;
        _call = request.getCall();
        _branch = branch;
        _cancel = request.isCancel();
            
        _key = _cancel ? "cancel-" + branch : branch;
        request.setTransaction(this);
    }
    
    public void setCall(Call call)
    {
    	_call = call;
    }
    
    protected Transaction() { }
    
    public boolean isInvite() 
    {
        return _request.isInvite(); 
    }
    
    public boolean isAck() 
    {
        return _request.isAck();
    }
    
    public String getKey() 
    {
        return _key;
    }
    
    public SipRequest getRequest() 
    {
        return _request;
    }
    
    public int getState() 
    {
        return _state;
    }
    
    public boolean isCompleted()
    {
    	return (_state >= STATE_COMPLETED);
    }
    
    public abstract boolean isServer();
    
    public void setState(int state) 
    {
        if (Log.isDebugEnabled()) 
            Log.debug("{} -> {}", this, STATES[state]);
        
        _state = state;
    }
    
    public void startTimer(int timer, long delay)
    {
    	TimerTask timerTask = _timers[timer];
    	if (timerTask != null)
    		_call.cancel(timerTask);
    	_timers[timer] = _call.schedule(new Timer(timer), delay);
    }
    
    public void cancelTimer(int timer) 
    {
    	TimerTask timerTask = _timers[timer];
    	if (timerTask != null)
    		_call.cancel(timerTask);
    	_timers[timer] = null;
    }
    
    public abstract boolean isTransportReliable();
    
    public Server getServer() 
    {
        return getCall().getServer();
    }
    
    public String getBranch() 
    {
        return _branch;
    }
    
    public Call getCall() 
    {
        return _call;
    }
    
    public abstract void timeout(int id);
    
    class Timer implements Runnable, Serializable
    {
		private static final long serialVersionUID = 1L;
	
		private int _timer;
    	
    	public Timer(int timer)
    	{
    		_timer = timer;
    	}
    	
    	public void run()
    	{
    		try 
    		{		
    			timeout(_timer);
    		}
    		catch (Throwable t)
    		{
    			Log.debug(t);
    		}
    	}
    	
    	public String toString()
    	{
    		return "timer" + (char) (0x40 + _timer);
    	}
    }
  
    public String toString() 
    {
        return _branch + "/" + _request.getMethod() + "/" + STATES[_state];
    }
}

