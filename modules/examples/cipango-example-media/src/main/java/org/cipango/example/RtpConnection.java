package org.cipango.example;

import java.util.List;

public class RtpConnection {

    private String _host;
    private int _port;
    private List<Integer> _payloadTypes;

    public RtpConnection(String host, int port, List<Integer> payloadTypes)
    {
        _host = host;
        _port = port;
        _payloadTypes = payloadTypes;
    }

    public String getHost()
    {
        return _host;
    }

    public int getPort()
    {
        return _port;
    }

    public List<Integer> getPayloadTypes() {
        return _payloadTypes;
    }

}
