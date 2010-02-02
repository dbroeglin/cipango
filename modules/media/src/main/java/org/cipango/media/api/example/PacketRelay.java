package org.cipango.media.api.example;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.cipango.media.api.LifeCycle;
import org.cipango.media.api.MediaFactory;
import org.cipango.media.api.NoObjectAvailableException;
import org.cipango.media.api.RtpListener;
import org.cipango.media.api.RtpPacket;
import org.cipango.media.api.RtpReader;
import org.cipango.media.api.RtpSender;

public class PacketRelay implements RtpListener, LifeCycle
{

    private int _sourcePort;
    private InetAddress _destAddress;
    private int _destPort;

    private RtpReader _rtpReader;
    private RtpSender _rtpSender;

    public PacketRelay(int sourcePort, InetAddress destAddress, int destPort)
    {
        _sourcePort = sourcePort;
        _destAddress = destAddress;
        _destPort = destPort;
    }

    public void init() throws NoObjectAvailableException
    {
        _rtpReader = MediaFactory.getRtpReader(_sourcePort);
        _rtpReader.addRtpListener(this);
        _rtpSender = MediaFactory.getRtpSender(_destAddress, _destPort);
    }

    @Override
    public void start()
    {
        _rtpReader.start();
    }

    @Override
    public void stop()
    {
        _rtpReader.stop();
    }

    @Override
    public void receivedRtpPacket(RtpPacket rtpPacket)
    {
        _rtpSender.send(rtpPacket.getData());
    }

    public static void main(String[] args) {
        if (args.length != 3)
        {
            System.err.println(
                    "usage: java source_port dest_address dest_port");
            System.exit(1);
        }
        int sourcePort = Integer.parseInt(args[0]);
        InetAddress destAddress;
        try {
            destAddress = InetAddress.getByName(args[1]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }
        int destPort = Integer.parseInt(args[2]);
        PacketRelay packetRelay = new PacketRelay(sourcePort, destAddress,
                destPort);
        try
		{
			packetRelay.init();
		}
		catch (NoObjectAvailableException e)
		{
			e.printStackTrace();
			return;
		}
        packetRelay.start();
        try
        {
            Thread.sleep(10000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            packetRelay.stop();
        }
    }

}
