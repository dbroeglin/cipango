package org.cipango.util;

import java.util.ArrayList;

/**
 * List of {@see TimerTask}
 */
public class TimerList extends ArrayList<TimerTask>
{
	private static final long serialVersionUID = 1L;

	public TimerList()
	{
		super(3);
	}

	public synchronized void addTimer(TimerTask timer)
	{
		int index = 0;
		while (index < size() && (get(index).compareTo(timer) < 0))
			index++;
		add(index, timer);
	}
	
	public synchronized TimerTask getExpired(long time)
	{
		if (size() != 0)
		{
			TimerTask timer = get(0);
			if (timer.getExecutionTime() <= time)
				return remove(0);
		}
		return null;
	}
	
	public synchronized TimerTask peek()
	{
		return size() != 0 ? get(0) : null;
	}
}
