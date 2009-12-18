package org.cipango.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SdpParser {

    public RtpConnection getRtpConnection(String sdp)
    {
        StringReader stringReader = new StringReader(sdp);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        String host = null;
        List<Integer> payloadTypes = new ArrayList<Integer>();
        int port = -1;
        String line;
        try
        {
            while ((line = bufferedReader.readLine()) != null)
            {
                if (line.startsWith("c="))
                {
                    String[] c = line.split(" ");
                    if (c.length < 3)
                        return null;
                    host = c[c.length - 1];
                }
                if (line.startsWith("m=audio"))
                {
                    String[] m = line.split(" ");
                    for (String element: m)
                    {
                        try
                        {
                            int integer = Integer.parseInt(element);
                            if (port == -1)
                                port = integer;
                            else
                                payloadTypes.add(integer);
                        }
                        catch (NumberFormatException e) { }
                    }
                }
            }
        }
        catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
        try
        {
            InetAddress.getByName(host);
        }
        catch (UnknownHostException e)
        {
            return null;
        }
        if (host == null || port == -1 || payloadTypes.size() == 0)
            return null;
        return new RtpConnection(host, port, payloadTypes);
    }

}
