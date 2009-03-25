package org.cipango.kaleo.event;

import java.util.TimerTask;

public interface TimedValue<S, T extends TimerTask> 
{
	S getValue();
	
	T getTask();
	
	void resetTask(T task);
	
	void closeTask();
}
