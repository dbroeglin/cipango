package org.cipango.util;
import static junit.framework.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class TimerListTest
{	
	@Test
	public void testOrder()
	{
		TimerList list = new TimerList();
		
		TimerTask[] tasks = new TimerTask[1000];
		Random random = new Random();
		for (int i = 0; i < tasks.length; i++)
		{
			tasks[i] = new TimerTask(null, Math.abs(random.nextLong()));
			list.addTimer(tasks[i]);
		}
		
		long time = -1;
		
		for (TimerTask task : list)
		{
			assertTrue(task.getExecutionTime() >= time);
			time = task.getExecutionTime();
		}
		
		time = Math.abs(random.nextLong());
		
		TimerTask task = null;
		while ((task = list.getExpired(time)) != null)
		{
			assertTrue(task.getExecutionTime() <= time);
		}
		
		while (list.size() > 0)
		{
			assertTrue(list.remove(0).getExecutionTime() > time);
		}
	} 
}
