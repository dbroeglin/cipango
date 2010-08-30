package org.cipango.diameter.io;

import java.io.IOException;

import org.cipango.diameter.DiameterAnswer;
import org.cipango.diameter.DiameterCommand;
import org.cipango.diameter.DiameterMessage;
import org.cipango.diameter.DiameterRequest;
import org.cipango.diameter.Dictionary;
import org.cipango.diameter.Factory;
import org.cipango.diameter.base.Common;
import org.cipango.diameter.util.BufferUtil;
import org.eclipse.jetty.io.Buffer;

/**
 * <pre>
 *  0                   1                   2                   3
 *  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |    Version    |                 Message Length                |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * | command flags |                  Command-Code                 |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |                         Application-ID                        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |                      Hop-by-Hop Identifier                    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |                      End-to-End Identifier                    |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+ 
 * |  AVPs ... 
 * +-+-+-+-+-+-+-+-+-+-+-+-+-
 * </pre>
 */
public class MessageCodec extends AbstractCodec<DiameterMessage>
{
	public static final int DIAMETER_VERSION_1 = 1;
	public static final int REQUEST_FLAG = 0x80;
	public static final int PROXIABLE_FLAG = 0x40;
	
	public DiameterMessage decode(Buffer buffer) throws IOException
	{
		int i = BufferUtil.getInt(buffer);
		int version = i >> 24 & 0xff;
		
		if (version != DIAMETER_VERSION_1)
			throw new IOException("Unsupported diameter version: " + version);
		
		i = BufferUtil.getInt(buffer);
		
		int flags = i >> 24 & 0xff;
		boolean isRequest = ((flags & REQUEST_FLAG) == REQUEST_FLAG);
		
		int code = i & 0xffffff;
		
		Dictionary dictionary = Dictionary.getInstance();
		
		DiameterCommand command = isRequest ? dictionary.getRequest(code) : dictionary.getAnswer(code);
		if (command == null)
			command = isRequest ? Factory.newRequest(code, "Unknown") : Factory.newAnswer(code, "Unknown");
		
		DiameterMessage message = isRequest ? new DiameterRequest() : new DiameterAnswer();
		
		message.setApplicationId(getInt(buffer));
		message.setHopByHopId(getInt(buffer));
		message.setEndToEndId(getInt(buffer));
		message.setCommand(command);
		
		if (isRequest)
			((DiameterRequest) message).setUac(false);
		
		message.setAVPList(Common.__grouped.decode(buffer));
		return message;
	}
	
	public Buffer encode(Buffer buffer, DiameterMessage message) throws IOException
	{
		int start = buffer.putIndex();
		buffer.setPutIndex(start+4);
		
		int flags = 0;
		DiameterCommand command = message.getCommand();
		
		if (command.isRequest())
			flags |= REQUEST_FLAG;
		
		if (command.isProxiable())
			flags |= PROXIABLE_FLAG;
		
		putInt(buffer, flags << 24 | command.getCode() & 0xffffff);
		putInt(buffer, message.getApplicationId());
		putInt(buffer, message.getHopByHopId());
		putInt(buffer, message.getEndToEndId());
		
		buffer = Common.__grouped.encode(buffer, message.getAVPs());
		pokeInt(buffer, start, DIAMETER_VERSION_1 << 24 | (buffer.putIndex() - start) & 0xffffff);
		
		return buffer;
	}
}
