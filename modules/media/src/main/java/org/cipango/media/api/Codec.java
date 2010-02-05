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

import org.mortbay.io.Buffer;

/**
 * Audio Coder/Decoder.
 * 
 * A codec just takes a jetty 6
 * <a href="http://jetty.codehaus.org/jetty/jetty-6/apidocs/index.html?org/mortbay/io/Buffer.html"
 *   target="_blank">
 * Buffer</a> as input and generates an output as
 * another Buffer. This Buffer can be either encoded or decoded. Each Codec
 * implementation will probably have its own set of parameters, but each one
 * will {@link #encode(Buffer)} and {@link #decode(Buffer)} bytes. The main
 * aim of a codec is to compress raw samples (which requires much space) to a
 * tiny version, losing fewest data as possible.
 * 
 * @author yohann
 */
public interface Codec
{

	/**
	 * Encodes data from raw samples to compressed format.
	 * 
	 * @param buffer raw samples, with predefined raw format.
	 * @return a distinct Buffer containing compressed data.
	 */
    public Buffer encode(Buffer buffer);

    /**
     * Takes compressed data as input, and generate uncompressed data. This
     * uncompressed data will probably be employed by another object to perform
     * something (play back to user, filter data, etc.).
     * 
     * @param buffer compressed data
     * @return a distinct Buffer containing uncompressed data.
     */
    public Buffer decode(Buffer buffer);

}
