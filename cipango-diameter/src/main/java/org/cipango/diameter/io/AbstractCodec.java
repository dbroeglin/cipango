package org.cipango.diameter.io;

import org.eclipse.jetty.io.Buffer;
import org.eclipse.jetty.io.ByteArrayBuffer;

public abstract class AbstractCodec<T> implements DiameterCodec<T> 
{
	public Buffer ensureSpace(Buffer buffer, int space)
	{
		if (buffer.space() < space)
		{
			while (space < (buffer.capacity() / 2) && space < 128)
				space *= 2;
			ByteArrayBuffer larger = new ByteArrayBuffer(buffer.capacity() + space);
			larger.put(buffer);
			return larger;
		}
		return buffer;
	}
	
	public int getInt(Buffer buffer)
	{ 
		return (buffer.get() & 0xff) << 24 
			| (buffer.get() & 0xff) << 16 
			| (buffer.get() & 0xff) << 8
			| (buffer.get() & 0xff);
	}
	
	public Buffer putInt(Buffer buffer, int value)
	{ 
		buffer.put((byte)  (value >> 24 & 0xff));
		buffer.put((byte)  (value >> 16 & 0xff));
		buffer.put((byte)  (value >> 8 & 0xff));
		buffer.put((byte)  (value & 0xff));
		return buffer;
	}	
	
	public Buffer pokeInt(Buffer buffer, int index, int value)
	{
		buffer.poke(index, (byte)  (value >> 24 & 0xff));
		buffer.poke(index+1, (byte)  (value >> 16 & 0xff));
		buffer.poke(index+2, (byte)  (value >> 8 & 0xff));
		buffer.poke(index+3, (byte)  (value & 0xff));
		return buffer;
	}
}
