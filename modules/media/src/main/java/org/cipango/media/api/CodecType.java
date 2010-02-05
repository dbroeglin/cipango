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

/**
 * Enumerates data format that can be handled by media API.
 * 
 * PCMU and PCMA correspond to <a target="_blank"
 * href="http://www.itu.int/rec/dologin_pub.asp?lang=e&id=T-REC-G.711-198811-I!!PDF-E&type=items">
 * ITU-T G.711</a>, respectively mu-law and A-law versions.
 * <p>
 * PCM_1S16LE8000 raw data format name can be decoded as follows:
 * <table>
 *   <tr>
 *     <th>1</th>
 *     <td>mono-channel</td>
 *   </tr>
 *   <tr>
 *     <th>S</th>
 *     <td>signed samples</td>
 *   </tr>
 *   <tr>
 *     <th>16</th>
 *     <td>16 bits samples</td>
 *   </tr>
 *   <tr>
 *     <th>LE</th>
 *     <td>little-endian samples</td>
 *   </tr>
 *   <tr>
 *     <th>8000</th>
 *     <td>sample rate is 8000 Hz</td>
 *   </tr>
 * </table>
 * 
 * @author yohann
 */
public enum CodecType
{
	PCMU,
    PCMA,
    PCM_1S16LE8000 
}
