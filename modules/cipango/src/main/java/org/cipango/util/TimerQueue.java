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

package org.cipango.util;

public class TimerQueue //<T extends TimerQueue.Node>
{
	public static final int DEFAULT_INITIAL_CAPACITY = 11;
	
	private Node[] _heap;
	protected int _size;
	
	public TimerQueue()
	{
		this(DEFAULT_INITIAL_CAPACITY);
	}
	
	public TimerQueue(int initialCapacity)
	{
		_heap = new Node[initialCapacity];
	}
	
	public int getSize()
	{
		return _size;
	}
	
	public void offer(Node node)
	{
		if (node == null)
			throw new NullPointerException();
		if (node._index > 0)
			remove(node);
		if (++_size == _heap.length)
		{
			Node[] newHeap = new Node[_heap.length*2];
			System.arraycopy(_heap, 0, newHeap, 0, _heap.length);
			_heap = newHeap;
		}
		if (_size == 1)
			set(_size, node);
		else
			siftUp(_size, node);
		
		assert invariant();
	}
	
	public void offer(Node node, long value)
	{
		if (node._index > 0)
		{
			if (_heap[node._index] != node)
				throw new IllegalArgumentException("invalid node: " + node);
			long oldValue = node._value;
			node._value = value;
			
			if (value > oldValue)
				siftDown(node._index, node);
			else
				siftUp(node._index, node);
		}
		else
		{
			node._value = value;
			offer(node);
		}
		
		assert invariant();
	}
	
	public Node peek()
	{
		return _size > 0 ? _heap[1] : null;
	}
	
	public Node poll()
	{
		if (_size == 0)
			return null;
		Node node = _heap[1]; 
		node._index = -1;
		
		Node last = _heap[_size];
		_heap[_size--] = null;
		
		if (_size != 0)
			siftDown(1, last);
		
		assert invariant();
		
		return node;
	}
	
	public void remove(Node node)
	{
		if (node._index != -1)
		{
			if (_heap[node._index] != node)
				throw new IllegalArgumentException("invalid node: " + node);
			removeAt(node._index);
		}
		node._index = -1;
		
		assert invariant();
	}
	
	private void removeAt(int i)
	{
		assert i > 0 && i <= _size;
		
		if (i == _size)
		{
			_heap[_size--] = null;
		}
		else
		{
			Node node = _heap[_size];
			_heap[_size--] = null;
			siftDown(i, node);
			if (_heap[i] == node)
				siftUp(i, node);
		}
	}
	
	private void siftUp(int i, Node node)
	{
		assert i > 0 && i <= _size;

		while (i>1)
		{
			int j = i >>> 1;
			Node parent = _heap[j];
			if (parent._value <= node._value)
				break;
			set(i, parent);
			i = j;
		}
		set(i, node);
	}
	
	private void siftDown(int i, Node node)
	{
		assert i > 0 && i <= _size;

		int j;
		while ((j = i << 1) <= _size)
		{
			Node child = _heap[j];
			if (j < _size && child._value > _heap[j+1]._value)
				child = _heap[++j];
			if (node._value <= child._value)
				break;
			set(i, child);
			i = j;
		}
		set(i, node);
	}
	
	private final void set(int k, Node node)
	{
		_heap[k] = node;
		node._index = k;
	}
	
	protected Node[] asArray()
	{
		Node[] nodes = new Node[_size];
		System.arraycopy(_heap, 1, nodes, 0, _size);
		return nodes;
	}
	
	public boolean invariant()
	{
		for (int i = _size; i > 1; i--)
		{
			Node node = _heap[i];
			Node parent = _heap[i/2];
			
			if (parent._value > node._value)
				return false;
		}
		return true;
	}
	
	public static class Node
	{
		private int _index = -1;
		protected long _value;
		
		public Node()
		{
			this(Long.MAX_VALUE);
		}
		
		public Node(long value)
		{
			_value = value;
		}
		
		public long getValue()
		{
			return _value;
		}
	}
}
