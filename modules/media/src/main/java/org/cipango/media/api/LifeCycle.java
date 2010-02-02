package org.cipango.media.api;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * A LifeCycle is something that can be started and stopped.
 * 
 * Objects implementing this interface will probably trigger tasks on
 * background threads, either using {@link ExecutorService} or
 * {@link ScheduledExecutorService} referenced by {@link MediaFactory}.
 * 
 * @author yohann
 */
public interface LifeCycle
{

	/**
	 * Starts this LifeCycle.
	 */
	public void start();

	/**
	 * Stops this LifeCycle.
	 */
	public void stop();

}
