package org.cipango.media.api;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
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
 * 
 * @author yohann
 */
public class MediaFactory {

	/**
	 * Retrieve an instance of {@link RtpReader} providing local port on which
	 * rtp packets should be read. As no IP address is provided,
	 * {@link RtpReader} will listen on all interfaces.
	 * 
	 * @param port the port number on which packets will be received.
	 * @return an {@link RtpReader} that will listen on all interfaces.
	 * @throws NoObjectAvailableException if no {@link RtpReader} is available.
	 */
	public static RtpReader getRtpReader(int port)
		throws NoObjectAvailableException
	{
		return null;
	}

	/**
	 * Retrieve an instance of {@link RtpReader} providing local port and
	 * IP address on which packets should be read. This method is useful if
	 * you are using several IP addresses. Only packets received on the
	 * provided port and on the provided IP address will be processed.
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
        return null;
    }

    /**
     * Retrieve an instance of {@link RtpSender} to send packets to the
     * destination IP address and port provided. The local port on which
     * the underlying socket will bound will be random.
     * 
     * @param destAddress the IP address on which packet should be sent.
     * @param destPort the destination port number on which packet should be
     * sent.
     * @return an {@link RtpSender} that send packets to provided address and
     * port.
     * @throws NoObjectAvailableException if no {@link RtpSender} is available.
     */
    public static RtpSender getRtpSender(InetAddress destAddress, int destPort)
    	throws NoObjectAvailableException
    {
        return null;
    }

    /**
     * Retrieve an instance of {@link RtpSender} to send packets to the
     * destination IP address and port provided. The local port on which
     * the underlying socket will bound is provided as a parameter. This method
     * can be useful with some firewalls or in any case where you have to
     * manage local ports yourself.
     * 
     * @param destAddress the IP address on which RTP packets will be sent.
     * @param destPort the port number on which RTP packets will be sent.
     * @param localPort the local port on which the socket will bound.
     * @return an instance of {@link RtpSender} that will send packets using
     * a given local port number.
     * @throws NoObjectAvailableException if no {@link RtpSender} is available.
     */
    public static RtpSender getRtpSender(InetAddress destAddress, int destPort,
        int localPort) throws NoObjectAvailableException
    {
        return null;
    }

    /**
     * Retrieve an instance of {@link RtpSender} to send packets to the
     * destination IP address and port provided. The local port and local
     * IP address on which the underlying socket will bound are provided as
     * parameters. This method is useful when your using several IP addresses,
     * if you want to sort packets on a given interface and if you want to
     * manage local port bindings yourself.
     * 
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
    public static RtpSender getRtpSender(InetAddress destAddress, int destPort,
            InetAddress localAddress, int localPort)
    		throws NoObjectAvailableException
    {
        return null;
    }

    /**
     * Retrieve an instance of {@link FileReader} corresponding to the provided
     * file name. Only one thread can use the same file reader at the same
     * instant.
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
        return null;
    }

    /**
     * Retrieve an instance of {@link FileWriter} to write files to the
     * provided file name. A new file will be created. Several threads can
     * write to the same {@link FileWriter} at the same time.
     * 
     * @param filename the file name of the file to create. 
     * @return a instance of {@link FileWriter} that will write bytes to the
     * file which name is provided.
     * @throws NoObjectAvailableException if no {@link FileWriter} is free.
     */
    public static FileWriter getFileWriter(String filename)
    	throws NoObjectAvailableException
    {
        return null;
    }

    /**
     * Retrieve an instance of {@link Mixer}. No parameter is necessary
     * to retrieve a {@link Mixer} instance. A {@link Mixer} will just
     * take n input will only give one output.
     * 
     * @return a {@link Mixer} instance.
     * @throws NoObjectAvailableException if no {@link Mixer} is available.
     */
    public static Mixer getMixer() throws NoObjectAvailableException
    {
        return null;
    }

    /**
     * Retrieve an instance of {@link DtmfFilter}. No parameter is necessary
     * to retrieve a {@link DtmfFilter} instance. A {@link DtmfFilter} will
     * be useful if your application has to detect DTMF events. This method
     * does not throw {@link NoObjectAvailableException} because it's not
     * linked to a system resource. It's pure software.
     * 
     * @return a {@link DtmfFilter} instance.
     */
    public static DtmfFilter getDtmfFilter()
    {
    	return null;
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
        return null;
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
        return null;
    }

}
