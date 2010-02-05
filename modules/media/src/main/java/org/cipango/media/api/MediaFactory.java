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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * MediaFactory is the access point of this API.
 * 
 * It's the factory that will create and keep references to all objects in
 * this API. It can be configured by external means (xml, jmx, etc.).
 * MediaFactory is configured so that a fixed number of objects is created
 * at the beginning and then, those objects are reserved/freed. Thus, no object
 * is created during API usage. This improves this API performance.
 * <p>
 * Objects provided by this API are: {@link RtpReader}, {@link RtpSender},
 * {@link FileReader}, {@link FileWriter}, {@link Mixer},
 * {@link DtmfFilter}, {@link ExecutorService} and
 * {@link ScheduledExecutorService}.
 * <p>
 * ExecutorService and ScheduledExecutorService are employed to execute tasks
 * in separate threads, using standard java 1.5 features.
 * <p>
 * Some methods of this class can throw {@link NoObjectAvailableException}
 * if all instances of a given type have already been taken. If this exception
 * is thrown, the same method can be invoked later, with the hope that this
 * type of object has been released.
 * <p>
 * All objects accessed using {@link MediaFactory} are {@link Managed} objects.
 * <p>
 * For RtpReaders, the same getRtpReader method must always be used. Either
 * interfaces are managed by application using configuration means, either
 * cipango media api listens on all interfaces, but a mix is not advised.
 * 
 * @author yohann
 */
public class MediaFactory
{

	/** Default Port for resource initialization. */
	public static final int DEFAULT_PORT = -1;

	//TODO make max values configurable
	public static final int MAX_RTP_READERS = 100;
	public static final int MAX_RTP_SENDERS = 100;
	public static final int MAX_FILE_READERS = 100;
	public static final int MAX_FILE_WRITERS = 100;
	public static final int MAX_DTMF_FILTERS = 100;
	public static final int SCHEDULED_THREADS = 10;
	public static final int MAX_DATAGRAM_SOCKETS = 100;
	public static final int MAX_SOURCE_SYNCHRONIZERS = 100;

	private static HashMap<String, RtpReader> __rtpReaders =
		new HashMap<String, RtpReader>();
	private static HashMap<String, RtpSender> __rtpSenders =
		new HashMap<String, RtpSender>();
	private static HashMap<String, FileReader> __fileReaders =
		new HashMap<String, FileReader>();
	private static HashMap<String, FileWriter> __fileWriters =
		new HashMap<String, FileWriter>();
	private static Mixer __mixer = new Mixer();
	private static HashMap<String, DtmfFilter> __dtmfFilters =
		new HashMap<String, DtmfFilter>();
	private static ExecutorService __executorService =
		Executors.newCachedThreadPool();
	private static ScheduledExecutorService __scheduledExecutorService =
		Executors.newScheduledThreadPool(SCHEDULED_THREADS);
	private static HashMap<String, DatagramSocket> __datagramSockets =
		new HashMap<String, DatagramSocket>();
	private static HashMap<String, UdpEndPoint> __udpEndPoints =
		new HashMap<String, UdpEndPoint>();
	private static RtpParser __rtpParser = new RtpParser();
	private static Random __random = new Random();
	private static HashMap<String, SourceSynchronizer> __sourceSynchronizers =
		new HashMap<String, SourceSynchronizer>();

	/**
	 * Retrieve an instance of {@link RtpReader} providing local port on which
	 * rtp packets should be read. As no IP address is provided,
	 * {@link RtpReader} will listen on all interfaces. If this method has
	 * already been invoked with the same port, the same RtpReader will be
	 * returned. No new RtpReader will be created.
	 * 
	 * @param port the port number on which packets will be received.
	 * @return an {@link RtpReader} that will listen on all interfaces.
	 * @throws NoObjectAvailableException if no {@link RtpReader} is available.
	 */
	public static RtpReader getRtpReader(int port)
		throws NoObjectAvailableException
	{
		String key = String.valueOf(port);
		RtpReader rtpReader = __rtpReaders.get(key);
		if (rtpReader == null)
			if (__rtpReaders.size() < MAX_RTP_READERS)
			{
				rtpReader = new RtpReader(port);
				__rtpReaders.put(key, rtpReader);
			}
			else
				throw new NoObjectAvailableException(RtpReader.class);
		return rtpReader;
	}

	/**
	 * Retrieve an instance of {@link RtpReader} providing local port and
	 * IP address on which packets should be read. This method is useful if
	 * you are using several IP addresses. Only packets received on the
	 * provided port and on the provided IP address will be processed. If this
	 * method has already been invoked with the same port, the same RtpReader
	 * will be returned. No new RtpReader will be created.
	 * 
	 * @param inetAddress local address on which packets should be received.
	 * @param port port on which packets will be received.
	 * @return an instance of {@link RtpReader} that listens only on the
	 * provided ip address and port.
	 * @throws NoObjectAvailableException if no {@link RtpReader} is available.
	 */
    public static RtpReader getRtpReader(InetAddress inetAddress, int port)
    	throws NoObjectAvailableException
    {
    	String key = port + "_" + inetAddress.getHostAddress();
		RtpReader rtpReader = __rtpReaders.get(key);
		if (rtpReader == null)
			if (__rtpReaders.size() < MAX_RTP_READERS)
			{
				rtpReader = new RtpReader(inetAddress, port);
				__rtpReaders.put(key, rtpReader);
			}
			else
				throw new NoObjectAvailableException(RtpReader.class);
		return rtpReader;
    }

	/**
	 * Removes an RtpReader. The reference kept to this RtpReader will be
	 * dropped. Take care, if this RtpReader is used by several objects
	 * in your application, make sure that all objects do not need this
	 * RtpReader anymore.
	 * 
	 * @param rtpReader the rtpReader that is not necessary anymore.
	 */
	public static void removeRtpReader(RtpReader rtpReader)
	{
		String key = String.valueOf(rtpReader.getPort());
		InetAddress inetAddress = rtpReader.getInetAddress();
		if (inetAddress != null)
			key += "_" + inetAddress.getHostAddress();
		__rtpReaders.remove(key);
	}

    /**
     * Retrieve an instance of {@link RtpSender} to send packets to the
     * destination IP address and port provided. The local port on which
     * the underlying socket will bound will be random. If this method has
	 * already been invoked with the same port, the same RtpSender will be
	 * returned. No new RtpSender will be created.
     * 
     * @param payloadType the SDP payload type that will be used for RTP
     * packets.
     * @param destAddress the IP address on which packet should be sent.
     * @param destPort the destination port number on which packet should be
     * sent.
     * @return an {@link RtpSender} that send packets to provided address and
     * port.
     * @throws NoObjectAvailableException if no {@link RtpSender} is available.
     */
    public static RtpSender getRtpSender(int payloadType,
    		InetAddress destAddress, int destPort)
    	throws NoObjectAvailableException
    {
    	String key = destPort + "_" + destAddress.getHostAddress();
		RtpSender rtpSender = __rtpSenders.get(key);
		if (rtpSender == null)
			if (__rtpSenders.size() < MAX_RTP_SENDERS)
			{
				rtpSender = new RtpSender(payloadType, destAddress, destPort);
				__rtpSenders.put(key, rtpSender);
			}
			else
				throw new NoObjectAvailableException(RtpSender.class);
		return rtpSender;
    }

    /**
     * Retrieve an instance of {@link RtpSender} to send packets to the
     * destination IP address and port provided. The local port on which
     * the underlying socket will bound is provided as a parameter. This method
     * can be useful with some firewalls or in any case where you have to
     * manage local ports yourself. If this method has already been invoked
     * with the same port, the same RtpSender will be returned. No new RtpSender
     * will be created.
     * 
     * @param payloadType the SDP payload type that will be used for RTP
     * packets.
     * @param destAddress the IP address on which RTP packets will be sent.
     * @param destPort the port number on which RTP packets will be sent.
     * @param localPort the local port on which the socket will bound.
     * @return an instance of {@link RtpSender} that will send packets using
     * a given local port number.
     * @throws NoObjectAvailableException if no {@link RtpSender} is available.
     */
    public static RtpSender getRtpSender(int payloadType,
    		InetAddress destAddress, int destPort, int localPort)
    	throws NoObjectAvailableException
    {
    	String key = destPort + "_" + destAddress.getHostAddress() + "_"
    		+ localPort;
		RtpSender rtpSender = __rtpSenders.get(key);
		if (rtpSender == null)
			if (__rtpSenders.size() < MAX_RTP_SENDERS)
			{
				rtpSender = new RtpSender(payloadType, destAddress, destPort,
						localPort);
				__rtpSenders.put(key, rtpSender);
			}
			else
				throw new NoObjectAvailableException(RtpSender.class);
		return rtpSender;
    }

    /**
     * Retrieve an instance of {@link RtpSender} to send packets to the
     * destination IP address and port provided. The local port and local
     * IP address on which the underlying socket will bound are provided as
     * parameters. This method is useful when your using several IP addresses,
     * if you want to sort packets on a given interface and if you want to
     * manage local port bindings yourself. If this method has already been
     * invoked with the same port, the same RtpSender will be returned. No new
     * RtpSender will be created.
     * 
     * @param payloadType the SDP payload type that will be used for RTP
     * packets.
     * @param destAddress the IP address on which packets will be sent.
     * @param destPort the port number on which packets will be sent.
     * @param localAddress the local IP address on which the underlying socket
     * will bound.
     * @param localPort the local port on which the underlying socket will
     * bound.
     * @return an {@link RtpSender} instance that will send RTP packets to
     * the IP address and port provided.
     * @throws NoObjectAvailableException if no {@link RtpSender} instance is
     * available.
     */
    public static RtpSender getRtpSender(int payloadType,
    		InetAddress destAddress, int destPort, InetAddress localAddress,
    		int localPort)
    	throws NoObjectAvailableException
    {
		String key = destPort + "_" + destAddress.getHostAddress() + "_"
				+ localPort + "_" + localAddress.getHostAddress();
		RtpSender rtpSender = __rtpSenders.get(key);
		if (rtpSender == null)
			if (__rtpSenders.size() < MAX_RTP_SENDERS)
			{
				rtpSender = new RtpSender(payloadType, destAddress, destPort,
						localAddress, localPort);
				__rtpSenders.put(key, rtpSender);
			}
			else
				throw new NoObjectAvailableException(RtpSender.class);
		return rtpSender;
    }

	/**
	 * Removes an RtpSender. The reference kept to this RtpSender will be
	 * dropped. Take care, if this RtpSender is used by several objects
	 * in your application, make sure that all objects do not need this
	 * RtpSender anymore.
	 * 
	 * @param rtpSender the rtpSender that is not necessary anymore.
	 */
	public static void removeRtpSender(RtpSender rtpSender)
	{
		String key = String.valueOf(rtpSender.getRemotePort()) + "_"
			+ rtpSender.getRemoteAddress().getHostAddress();
		int localPort = rtpSender.getLocalPort();
		if (localPort != DEFAULT_PORT)
		{
			key += "_" + localPort;
			InetAddress inetAddress = rtpSender.getLocalAddress();
			if (inetAddress != null)
				key += "_" + inetAddress.getHostAddress();
		}
		__rtpSenders.remove(key);
	}

    /**
     * Retrieve an instance of {@link FileReader} corresponding to the provided
     * file name. Only one thread can use the same file reader at the same
     * instant. If this method has already been invoked with the same filename,
     * the same FileReader will be returned. No new FileReader will be created.
     * 
     * @param filename the name of the file to open for reading. This file name
     * can be absolute or relative the path where cipango has been started.
     * @return an instance of {@link FileReader} that will read pacekts from
     * the provided filename.
     * @throws NoObjectAvailableException if no {@link FileReader} is
     * available.
     */
    public static FileReader getFileReader(String filename)
    	throws NoObjectAvailableException
    {
    	FileReader fileReader = __fileReaders.get(filename);
    	if (fileReader == null)
			if (__fileReaders.size() < MAX_FILE_READERS)
			{
				fileReader = new FileReader(filename);
				__fileReaders.put(filename, fileReader);
			}
			else
				throw new NoObjectAvailableException(FileReader.class);
        return fileReader;
    }

	/**
	 * Removes a FileReader. The reference kept to this FileReader will be
	 * dropped. Take care, if this FileReader is used by several objects
	 * in your application, make sure that all objects do not need this
	 * FileReader anymore.
	 * 
	 * @param fileReader the FileReader that is not necessary anymore.
	 */
	public static void removeFileReader(FileReader fileReader)
	{
		__fileReaders.remove(fileReader.getFilename());
	}

    /**
     * Retrieve an instance of {@link FileWriter} to write files to the
     * provided file name. A new file will be created. Several threads can
     * write to the same {@link FileWriter} at the same time. If this method
     * has already been invoked with the same filename, the same FileWriter
     * will be returned. No new FileWriter will be created.
     * 
     * @param filename the file name of the file to create. 
     * @return a instance of {@link FileWriter} that will write bytes to the
     * file which name is provided.
     * @throws NoObjectAvailableException if no {@link FileWriter} is free.
     */
    public static FileWriter getFileWriter(String filename)
    	throws NoObjectAvailableException
    {
    	FileWriter fileWriter = __fileWriters.get(filename);
    	if (fileWriter == null)
			if (__fileWriters.size() < MAX_FILE_WRITERS)
			{
				fileWriter = new FileWriter(filename);
				__fileWriters.put(filename, fileWriter);
			}
			else
				throw new NoObjectAvailableException(FileWriter.class);
        return fileWriter;
    }

	/**
	 * Removes a FileWriter. The reference kept to this FileWriter will be
	 * dropped. Take care, if this FileWriter is used by several objects
	 * in your application, make sure that all objects do not need this
	 * FileWriter anymore.
	 * 
	 * @param fileWriter the FileWriter that is not necessary anymore.
	 */
	public static void removeFileWriter(FileWriter fileWriter)
	{
		__fileWriters.remove(fileWriter.getFilename());
	}

    /**
     * Retrieve an instance of {@link Mixer}. No parameter is necessary
     * to retrieve a {@link Mixer} instance. A {@link Mixer} will just
     * take n input will only give one output.
     * 
     * @return a {@link Mixer} instance.
     */
    public static Mixer getMixer()
    {
    	return __mixer;
    }

    /**
     * Retrieve an instance of {@link DtmfFilter}. One parameter must be
     * provided to retrieve a {@link DtmfFilter}: a key string. This key
     * string is just employed internally as a key to store a reference
     * to this DtmfFilter. It's not used to create the DtmfFilter. Thus, this
     * key must identically identify the DtmfFilter. One advise is to use
     * RTP SSRC field in this key. A {@link DtmfFilter} will be useful if
     * your application has to detect DTMF events.
     * 
     * @param key unique DtmfFilter identifier provided by application.
     * @return a {@link DtmfFilter} instance.
     * @throws NoObjectAvailableException if no {@link DtmfFilter} is free.
     */
    public static DtmfFilter getDtmfFilter(String key)
    	throws NoObjectAvailableException
    {
		DtmfFilter dtmfFilter = __dtmfFilters.get(key);
		if (dtmfFilter == null)
			if (__dtmfFilters.size() < MAX_DTMF_FILTERS)
			{
				dtmfFilter = new DtmfFilter();
				__dtmfFilters.put(key, dtmfFilter);
			}
			else
				throw new NoObjectAvailableException(DtmfFilter.class);
		return dtmfFilter;
    }

	/**
	 * Removes a DtmfFilter. The reference kept to this DtmfFilter will be
	 * dropped. Take care, if this DtmfFilter is used by several objects
	 * in your application, make sure that all objects do not need this
	 * DtmfFilter anymore.
	 * 
	 * @param key DtmfFilter id provided at object creation.
	 */
	public static void removeDtmfFilter(String key)
	{
		__dtmfFilters.remove(key);
	}

    /**
     * Retrieve the unique {@link ExecutorService} provided by this API.
     * This {@link ExecutorService} will be useful if you have to execute
     * background tasks. Those tasks can eventually last long. As only one
     * {@link ExecutorService} is provided by this API, it will always be
     * available and thus, this method does not throw
     * {@link NoObjectAvailableException}
     * 
     * @return the unique {@link ExecutorService}.
     */
    public static ExecutorService getExecutorService()
    {
    	return __executorService;
    }

    /**
     * Retrieve the unique {@link ScheduledExecutorService} provided by this
     * API. This {@link ScheduledExecutorService} will be useful if you have
     * to execute regular background tasks. Nevertheless, those tasks must
     * execute quickly. You can execute those tasks with either a fixed rate
     * or a fixed delay. Thus the behaviour will very depending on its usage.
     * 
     * @return the unique {@link ScheduledExecutorService}.
     */
    public static ScheduledExecutorService getScheduleExecutorService()
    {
        return __scheduledExecutorService;
    }

    /**
     * Retrieve a UdpEndPoint to receive UDP packets.
     * 
     * @return a {@link UdpEndPoint} that sends UDP packets.
     * @throws NoObjectAvailableException if no {@link UdpEndPoint} is free.
     */
    static UdpEndPoint getUdpEndPoint() throws NoObjectAvailableException
    {
    	UdpEndPoint udpEndPoint;
    	if (__datagramSockets.size() < MAX_DATAGRAM_SOCKETS)
    	{
    		DatagramSocket datagramSocket;
			try
			{
				datagramSocket = new DatagramSocket();
			}
			catch (SocketException e)
			{
				throw new NoObjectAvailableException(UdpEndPoint.class);
			}
    		String key = String.valueOf(datagramSocket.getLocalPort());
    		__datagramSockets.put(key, datagramSocket);
    		udpEndPoint = new UdpEndPoint(datagramSocket);
    		__udpEndPoints.put(key, udpEndPoint);
    	}
    	else
    		throw new NoObjectAvailableException(UdpEndPoint.class);
    	return udpEndPoint;
    }

    /**
     * Retrieve an instance of UdpEndPoint bounded on provided port and
     * InetAddress. InetAddress may be null. This end point will receive
     * UDP packets. If this method has already been invoked with the same
     * port and InetAddress, the same UdpEndPoint will be returned. No new
     * UdpEndPoint will be created.
     * 
     * @param port local port.
     * @param inetAddress may be null, bounds on specified address.
     * @return an instance of {@link UdpEndPoint} to send/receive packets.
     * @throws NoObjectAvailableException if no {@link UdpEndPoint} is free.
     */
    static UdpEndPoint getUdpEndPoint(int port, InetAddress inetAddress)
    	throws NoObjectAvailableException
    {
    	UdpEndPoint udpEndPoint;
    	if (__datagramSockets.size() < MAX_DATAGRAM_SOCKETS)
    	{
    		DatagramSocket datagramSocket;
			try
			{
				datagramSocket = new DatagramSocket(port, inetAddress);
			}
			catch (SocketException e)
			{
				throw new NoObjectAvailableException(UdpEndPoint.class);
			}
    		String key = datagramSocket.getLocalPort() + "_"
    			+ inetAddress.getHostAddress();
    		__datagramSockets.put(key, datagramSocket);
    		udpEndPoint = new UdpEndPoint(datagramSocket);
    		__udpEndPoints.put(key, udpEndPoint);
    	}
    	else
    		throw new NoObjectAvailableException(UdpEndPoint.class);
    	return udpEndPoint;
    }

	/**
	 * Removes a UdpEndPoint. The reference kept to this UdpEndPoint and
	 * to its underlying DatagramSocket will be dropped. Take care, if those
	 * UdpEndPoint and DatagramSocket are used by several objects in your
	 * application, make sure that all objects do not need them anymore.
	 * 
	 * @param udpEndPoint the UdpEndPoint that is not necessary anymore.
	 */
    static void removeUdpEndPoint(UdpEndPoint udpEndPoint)
    {
    	String key = String.valueOf(udpEndPoint.getLocalPort());
    	DatagramSocket datagramSocket = udpEndPoint.getDatagramSocket();
    	if (datagramSocket != null)
    	{
    		InetAddress inetAddress = datagramSocket.getLocalAddress();
    		if (inetAddress != null)
    		{
    			key += "_" + inetAddress.getHostAddress();
    		}
    	}
    	__datagramSockets.remove(key);
    	__udpEndPoints.remove(key);
    }

    /**
     * Retrieve an RtpParser instance. This RtpParser can be employed for
     * RTP packets parsing and also for RTP packets building.
     * 
     * @return an RtpParser instance.
     */
    public static RtpParser getRtpParser()
    {
    	return __rtpParser;
    }

    /**
     * Retrieve a Random instance.
     * 
     * @return a {@link Random} object.
     */
    public static Random getRandom()
    {
    	return __random;
    }

    /**
     * Retrieve an instance of {@link SourceSynchronizer}. One parameter
     * must be provided to retrieve a SourceSynchronizer: a key string. This
     * key string is just employed internally as a key to store a reference
     * to this SourceSynchronizer. It's not used to create the
     * SourceSynchronizer. Thus, this key must identically identify the
     * SourceSynchronizer. One advise is to use RTP SSRC field in this key.
     * A {@link SourceSynchronizer} will be useful if your application has
     * several source streams that must be synchronized and eventually mixed.
     * 
     * @param key unique SourceSynchronizer identifier provided by application.
     * @return a {@link SourceSynchronizer} instance.
     * @throws NoObjectAvailableException if no {@link SourceSynchronizer is
     * free.
     */
    public static SourceSynchronizer getSourceSynchronizer(String key)
    	throws NoObjectAvailableException
    {
		SourceSynchronizer sourceSynchronizer = __sourceSynchronizers.get(key);
		if (sourceSynchronizer == null)
			if (__sourceSynchronizers.size() < MAX_SOURCE_SYNCHRONIZERS)
			{
				sourceSynchronizer = new SourceSynchronizer();
				__sourceSynchronizers.put(key, sourceSynchronizer);
			}
			else
				throw new NoObjectAvailableException(SourceSynchronizer.class);
		return sourceSynchronizer;
    }

	/**
	 * Removes a SourceSynchronizer. The reference kept to this
	 * SourceSynchronizer will be dropped. Take care, if this
	 * SourceSynchronizer is used by several objects in your application,
	 * make sure that all objects do not need this SourceSynchronizer anymore.
	 * 
	 * @param key SourceSynchronizer id provided at object creation.
	 */
	public static void removeSourceSynchronizer(String key)
	{
		__sourceSynchronizers.remove(key);
	}

}
