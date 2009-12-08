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

package org.cipango.media.rtp;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;

import junit.framework.TestCase;


public class RtpDecoderTest extends TestCase
{
	protected Buffer load(String name) throws Exception
	{
		URL url = getClass().getClassLoader().getResource(name);
		File file = new File(url.toURI());
		FileInputStream fin = new FileInputStream(file);
		byte[] b = new byte[(int) file.length()];
		fin.read(b);
		
		return new ByteArrayBuffer(b);
	}
	
	public void testParse() throws Exception
	{
		Buffer buffer = load("rtp.dat");
		
		RtpPacket packet = new RtpDecoder().decode(buffer);
		assertEquals(160, packet.getData().length());
	}
}
