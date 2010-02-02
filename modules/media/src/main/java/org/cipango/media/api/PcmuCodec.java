package org.cipango.media.api;

import org.mortbay.io.Buffer;

/**
 * Encode/decode ITU-T G.711 packets using mu-law variant.
 * 
 * This codec is specified in the following documentation: <a target="_blank"
 * href="http://www.itu.int/rec/dologin_pub.asp?lang=e&id=T-REC-G.711-198811-I!!PDF-E&type=items">
 * ITU-T G.711</a>. It works on 8000Hz mono-channel samples.
 * 
 * @author yohann
 */
public class PcmuCodec implements Codec
{

	@Override
	public Buffer decode(Buffer buffer)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Buffer encode(Buffer buffer)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
