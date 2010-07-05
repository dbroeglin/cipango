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

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipServletMessage.HeaderForm;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.BufferUtil;
import org.eclipse.jetty.io.View;
import org.eclipse.jetty.io.BufferCache.CachedBuffer;

import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.http.HttpFields;

public class SipFields implements Cloneable
{    
	private Map<Buffer, Field> _fields = new LinkedHashMap<Buffer, Field>();
	
    @SuppressWarnings("unchecked")
	public static List qualityList(final Iterator it)
    {
    	return HttpFields.qualityList(
    			new Enumeration() 
    			{
					public boolean hasMoreElements()
					{
						return it.hasNext();
					}

					public Object nextElement()
					{
						return it.next();
					}
    			});
    }
    
    public SipFields() { }
    
    public void addAddress(Buffer name, Address value, boolean first)
    {
    	add(name, value, first);
    }
    
    public void addAddress(String name, Address value, boolean first)
    {
    	addAddress(SipHeaders.CACHE.lookup(name), value, first);
    }
    
    public void addParameterable(Buffer name, Parameterable value, boolean first)
    {
    	add(name, value, first);
    }
    
    public void addParameterable(String name, Parameterable value, boolean first)
    {
    	addParameterable(SipHeaders.CACHE.lookup(name), value, first);
    }
    
    public void addString(Buffer name, String value)
    {
    	add(name, value, false);
    }
    
    public void addString(String name, String value)
    {
    	add(SipHeaders.CACHE.lookup(name), value, false);
    }
    
    public void addVia(Via via, boolean first)
    {
    	add(SipHeaders.VIA_BUFFER, via, first);
    }
    
    public void addBuffer(Buffer name, Buffer value)
    {
    	if (!(name instanceof CachedBuffer))
    		name = SipHeaders.CACHE.lookup(name);
    	
    	Field field = (Field) _fields.get(name);
    	Field f = new Field(name, value);
    	
    	if (field == null)
    	{
    		_fields.put(f.getName(), f);
    	}
    	else
    	{
			while (field._next != null)
				field = field._next;
			field._next = f;
		}
    	
    }
    
    protected void add(Buffer name, Object value, boolean first)
    {
    	if (!(name instanceof CachedBuffer))
    		name = SipHeaders.CACHE.lookup(name);
    	
    	Field field = (Field) _fields.get(name);
    	Field f = new Field(name, value, 0); // TODO
    	
    	if (field == null)
    	{
    		_fields.put(f.getName(), f);
    	}
    	else
    	{
    		if (first)
    		{
    			f._next = field;
    			_fields.put(f.getName(), f);
    		}
    		else
    		{
    			while (field._next != null)
    				field = field._next;
    			field._next = f;
    		}
    	}
    }
    
    protected Field getField(Buffer name)
    {
    	return _fields.get(name);
    }
    
    public Address getAddress(Buffer name)
    {
    	Field field = getField(name);
    	if (field != null)
    		return field.getAddress();
    	return null;
    }
    
    public Address getAddress(String name)
    {
    	return getAddress(SipHeaders.CACHE.lookup(name));
    }
    
    public long getLong(Buffer name)
    {
    	Field field = getField(name);
    	if (field != null)
    		return field.getLong();
    	return -1l;
    }
    
    public long getLong(String name)
    {
    	return getLong(SipHeaders.CACHE.lookup(name));
    }
    
    public Parameterable getParameterable(Buffer name)
    {
    	Field field = getField(name);
    	if (field != null)
    		return field.getParameterable();
    	return null;
    }
    
    public Parameterable getParameterable(String name)
    {
    	return getParameterable(SipHeaders.CACHE.lookup(name));
    }
    
    public String getString(String name)
    {
    	return getString(SipHeaders.CACHE.lookup(name));
    }
    
    public String getString(Buffer name)
    {
    	Field field = getField(name);
    	if (field != null)
    		return field.getString();
    	return null;
    }
    
    public Via getVia()
    {
    	Field field = _fields.get(SipHeaders.VIA_BUFFER);
    	if (field != null)
    		return field.getVia();
    	return null;
    }
    
    public CSeq getCSeq() throws ServletParseException
    {
    	Field field = _fields.get(SipHeaders.CSEQ_BUFFER);
    	if (field != null)
    		return new CSeq(field.getString());
    	return null;
    }
    
    public ListIterator<String> getValues(Buffer name)
    {
    	Field field = getField(name);
    	
    	return new FieldIterator<String>(field)
    	{
    		public String getValue() { return _f.getString(); }
    	};
    }
    
    public ListIterator<String> getValues(String name)
    {
    	return getValues(SipHeaders.CACHE.lookup(name));
    }
    
    public ListIterator<Address> getAddressValues(Buffer name)
    {
    	Field field = getField(name);
    	
    	return new FieldIterator<Address>(field)
    	{
    		public Address getValue() { return _f.getAddress(); }
    	};
    }
    
    public ListIterator<Address> getAddressValues(String name)
    {
    	return getAddressValues(SipHeaders.CACHE.lookup(name));
    }
    
    public ListIterator<Parameterable> getParameterableValues(Buffer name)
    {
    	final Field field = getField(name);
    	
    	return new FieldIterator<Parameterable>(field)
    	{
    		public Parameterable getValue() { return _f.getParameterable(); }
    	};
    }
    
    public Iterator<Parameterable> getParameterableValues(String name)
    {
    	return getParameterableValues(SipHeaders.CACHE.lookup(name));
    }
    
    public Iterator<String> getNames()
    {
    	final Iterator<Buffer> it = _fields.keySet().iterator();
    	
    	return new Iterator<String>()
    	{
    		public boolean hasNext()
    		{
    			return it.hasNext();
    		}
    		public String next()
    		{
    			return it.next().toString();
    		}
    		public void remove()
    		{
    			throw new UnsupportedOperationException();
    		}
    	};
    }
    
    public void remove(Buffer name)
    {
    	_fields.remove(name);
    }
    
    public void remove(String name)
    {
    	_fields.remove(SipHeaders.CACHE.lookup(name));
    }
    
    public void removeFirst(Buffer name)
    {
    	Field f = getField(name);
    	
    	if (f == null) return;
    	
    	Field next = f._next;
    	if (next != null)
    		_fields.put(next.getName(), next);
    	else
    		_fields.remove(f.getName());
    }
    
    public void setAddress(Buffer name, Address value)
    {
    	set(name, value);
    }
    
    public void setAddress(String name, Address value)
    {
    	set(SipHeaders.CACHE.lookup(name), value);
    }
    
    public void setParameterable(Buffer name, Parameterable parameterable)
    {
    	set(name, parameterable);
    }
    
    public void setParameterable(String name, Parameterable parameterable)
    {
    	set(SipHeaders.CACHE.lookup(name), parameterable);
    }
    
    public void setString(Buffer name, String value)
    {
    	set(name, value);
    }
    
    public void setString(String name, String value)
    {
    	set(SipHeaders.CACHE.lookup(name), value);
    }
    
    protected void set(Buffer name, Object value)
    {
    	if (!(name instanceof CachedBuffer))
    		name = SipHeaders.CACHE.lookup(name);
    	
    	Field field = new Field(name, value, 0); // TODO
    	_fields.put(field.getName(), field);
    }

    static class Field
    {
    	private Field _next;
    	
    	private Buffer _name;
    	private Buffer _bvalue;
    	private Object _value;
    	
    	public Field(Buffer name, Buffer value)
    	{
    		_name = name;
    		_bvalue = value.isReadOnly() ? value : new View(value);
    	}
    	
    	public Field(Buffer name, Object value, int type)
    	{
    		_name = name;
    		_value = value;
    	}
    	
    	private Field(Buffer name)
    	{
    		_name = name;
    	}
    	
    	public Buffer getName()
    	{
    		return _name;
    	}
    	
    	public int getNameOrdinal()
    	{
    		return SipHeaders.CACHE.getOrdinal(_name);
    	}	
    	
    	public String getString()
    	{
    		if (_value == null)
    			_value = StringUtil.toUTF8String(_bvalue.array(), _bvalue.getIndex(), _bvalue.length());
    		
    		return _value.toString();
    	}
    	
    	public Address getAddress()
    	{
    		if (!(_value instanceof Address))
    		{
    			try
    			{
    				_value = new NameAddr(getString());
    				_bvalue = null;
    			}
    			catch (ServletParseException e)
    			{
    				throw new LazyParsingException(e);
    			}
    		}
    		return (Address) _value;
    	}
    	
    	public Parameterable getParameterable()
    	{
    		if (!(_value instanceof Parameterable))
    		{
    			try
    			{
    				_value = new ParameterableImpl(getString());
    				_bvalue = null;
    			}
    			catch (ServletParseException e)
    			{
    				throw new LazyParsingException(e);
    			}
    		}
    		return (Parameterable) _value;
    	}
    	
    	public Via getVia()
    	{
    		if (!(_value instanceof Via))
    		{
    			try
    			{
    				_value = new Via(getString());
    				_bvalue = null;
    			}
    			catch (ServletParseException e)
    			{
    				throw new LazyParsingException(e);
    			}
    		}
    		return (Via) _value;
    	}
    	
    	public long getLong()
    	{
    		if (_bvalue != null)
    			return BufferUtil.toLong(_bvalue);
    		else
    			return Long.parseLong(_value.toString());
    	}
    }
    
    public SipFields clone()
    {
    	SipFields clone = null;
    	try
    	{
    		clone = (SipFields) super.clone();
    	}
    	catch (CloneNotSupportedException _)
    	{
    	}
    	clone._fields = new HashMap<Buffer, Field>();
    	
    	for (Field field : _fields.values())
    	{
    		Field f = clone(field);
    		clone._fields.put(f.getName(), f);
    	}
    	return clone;
    }
    
    public void copy(SipFields other, Buffer name)
    {
    	Field field = other.getField(name);
    	if (field != null)
    	{
    		Field f = clone(field);
    		_fields.put(f._name, f);
    	}
    }
    
    private static Field clone(Field field)
    {
    	Field first = null;
    	Field previous = null;
		
    	while (field != null)
    	{
			Field f = new Field(field._name);
			f._bvalue = field._bvalue;
			Object value = field._value;
			if (value != null)
			{
				if (value instanceof Address)
					f._value = ((Address) value).clone();
				else if (value instanceof Via)
					f._value = ((Via) value).clone();
				else if (value instanceof Parameterable)
					f._value = ((Parameterable) value).clone();
				else if (value instanceof String)
					f._value = value;
				else 
					throw new RuntimeException("unexpected type: " + value.getClass());
			}
			if (previous != null)
				previous._next = f;
			else
				first = f;
			
			field = field._next;
			previous = f;
		}

		return first;
    }
    
    public static void put(Field field, Buffer buffer, HeaderForm form, boolean merge)
    {
    	Buffer name;
    	switch (form)
		{
		case COMPACT: 
			name = SipHeaders.getCompact(field.getName());
			break;
		default:
			name = field.getName();
			break;
		}
    	buffer.put(name);
		buffer.put((byte) ':');
		buffer.put((byte) ' ');
		
		boolean first = true;
		while (field != null)
		{
    		if (first)
    			first = false;
    		else if (merge)
    			buffer.put((byte) ',');
    		else
    		{
    			BufferUtil.putCRLF(buffer);
    			buffer.put(name);
    			buffer.put((byte) ':');
    			buffer.put((byte) ' ');
    		}
    		
    		if (field._bvalue != null)
    		{
    			buffer.put(field._bvalue);
    		}
    		else
    		{
    			try 
    			{
    				buffer.put(field._value.toString().getBytes(StringUtil.__UTF8));
    			} 
    		catch (UnsupportedEncodingException _) { }
    		}
    		field = field._next;
    	}
    	BufferUtil.putCRLF(buffer);
    }
    
    public Iterator<Field> getFields()
    {
    	return _fields.values().iterator();
    }
    
    abstract class FieldIterator<E> implements ListIterator<E> 
    {
    	Field _f;
		Field _first;
		int _index = 0;
		
		public FieldIterator(Field field)
		{
			_f = field;
			_first = field;
		}
		
		public boolean hasNext()
		{
			return _f != null;
		}
		
		public E next()
		{
			if (_f == null) throw new NoSuchElementException();
			
			E value = getValue();
			_f = _f._next;
			_index++;
			
			return value;
		}
		
		public abstract E getValue();
		
		public boolean hasPrevious() 
		{ 
			return _index > 0;
		}
		
		public E previous()
		{
			if (!hasPrevious()) throw new NoSuchElementException();
			
			_index--;
			
			_f = _first;
			for (int i = 0; i < _index; i++)
				_f = _f._next;
			
			return getValue();
		}
		
		public int nextIndex() { return _index; }
		public int previousIndex() { return _index-1; }
		
		public void set(E e) { throw new UnsupportedOperationException(); }
		public void add(E e) { throw new UnsupportedOperationException(); }
		public void remove() { throw new UnsupportedOperationException(); }
    }
}
