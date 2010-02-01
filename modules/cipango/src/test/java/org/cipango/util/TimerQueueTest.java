package org.cipango.util;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import org.cipango.util.TimerQueue;
import org.cipango.util.TimerQueue.Node;

import junit.framework.TestCase;

public class TimerQueueTest extends TestCase
{
	private Random _random = new Random();
	private TimerQueue _queue;
	
	@Override
	protected void setUp()
	{
		_queue = new TimerQueue();
	}
	
	void fillQueue(int nb)
	{
		for (int i = 0; i < nb; i++)
		{
			Node node = new Node(Math.abs(_random.nextLong()));
			_queue.offer(node);
		}
		assertEquals(nb, _queue.getSize());
	}
	
	public void testPriority()
	{
		fillQueue(10000);
		
		checkPriority();
	}
	
	void checkPriority()
	{
		long value = -1;
		while (_queue.getSize() > 0)
		{
			Node node = _queue.poll();
			assertTrue(value <= node.getValue());
			value = node.getValue();
		}
	}
	
	public void testFailed()
	{
		Node node = new Node(1);
		_queue.offer(node);
		_queue.remove(node);
		
	}

	public void testRemove()
	{
		for (int n = 0; n < 1000; n++)
		{
			fillQueue(100);
			
			Node[] nodes = _queue.asArray();
			
			for (int i = 0; i < 500; i++)
			{
				int j = _random.nextInt(nodes.length);
				Node node = nodes[j];
				nodes[j] = null;
				
				if (node != null)
					_queue.remove(node);
			}
			
			checkPriority();
			
			while (_queue.getSize() > 0)
				_queue.poll();
		}
	}
	
	public void testReschedule()
	{
		for (int n = 0; n < 1000; n++)
		{
			fillQueue(100);
			
			Node[] nodes = _queue.asArray();
			
			for (int i = 0; i < 500; i++)
			{
				int j = _random.nextInt(nodes.length);
				_queue.offer(nodes[j], Math.abs(_random.nextLong()));
			}
			
			checkPriority();
			
			while (_queue.getSize() > 0)
				_queue.poll();
		}
	}
	
	/*
	public void testPerfRemove()
	{
		int nb = 100000;
		
		fillQueue(nb);
		
		Node[] nodes = _queue.asArray();
		
		long start = System.currentTimeMillis();

		for (int i = 0; i < nb; i++)
		{
			int j = _random.nextInt(nodes.length);
			Node node = nodes[j];
			nodes[j] = null;
			
			if (node != null)
				_queue.remove(node);
		}
		
		System.out.println(System.currentTimeMillis() - start);
		
		
		PriorityQueue<Node> queue = new PriorityQueue<Node>(nb, 
				 new Comparator<Node>()
				 {
					public int compare(Node first, Node second) 
					{
						if (first == second)
							return 0;
						return (first.getValue() > second.getValue() ? 1 : -1);
					}
				 });
		
		for (int i = 0; i < nb; i++)
		{
			Node node = new Node(Math.abs(_random.nextLong()));
			queue.offer(node);
		}
		
		nodes = queue.toArray(nodes);
		
		start = System.currentTimeMillis();

		for (int i = 0; i < nb; i++)
		{
			int j = _random.nextInt(nodes.length);
			Node node = nodes[j];
			nodes[j] = null;
			
			if (node != null)
				queue.remove(node);
		}
		System.out.println(System.currentTimeMillis() - start);

	}
	*/
}
