package org.cipango.diameter.io;

import org.cipango.diameter.AVP;
import org.cipango.diameter.DiameterMessage;

/**
 * Diameter codec instances.
 */
public abstract class Codecs 
{
	private Codecs() {}
	
	public static final DiameterCodec<AVP<?>> __avp = new AVPCodec();
	public static final DiameterCodec<DiameterMessage> __message = new MessageCodec();
}
