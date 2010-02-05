// ========================================================================
// Copyright 2008-2010 NEXCOM Systems
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

package org.cipango.media.api;

import org.cipango.media.codecs.BasicCodecs;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

/**
 * Encode/decode ITU-T G.711 packets using A-law variant.
 * 
 * This codec is specified in the following documentation: <a target="_blank"
 * href="http://www.itu.int/rec/dologin_pub.asp?lang=e&id=T-REC-G.711-198811-I!!PDF-E&type=items">
 * ITU-T G.711</a>. It works on 8000Hz mono-channel samples.
 * 
 * @author yohann
 */
public class PcmaCodec implements Codec
{

	@Override
	public Buffer decode(Buffer buffer)
	{
		Buffer output = new ByteArrayBuffer(buffer.length() * 2);
		while (buffer.length() > 0)
		{
			int linear = BasicCodecs.alaw2linear(buffer.get());
			output.put((byte)(linear & 0xff));
			output.put((byte)((linear >> 8) & 0xff));
		}
		return output;
	}

	@Override
	public Buffer encode(Buffer buffer)
	{
		Buffer output = new ByteArrayBuffer(buffer.length() / 2);
		while (buffer.length() > 0)
		{
			byte firstByte = buffer.get();
			byte secondByte = buffer.get();
	        int pcm_val = (secondByte << 8) | (firstByte & 0xff);
	        output.put(BasicCodecs.linear2alaw(pcm_val));
		}
		return output;
	}

}
