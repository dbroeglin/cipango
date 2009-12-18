package org.cipango.example;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.media.Player;

public class MediaServlet extends SipServlet
{

    private static final long serialVersionUID = 1L;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        SdpParser sdpParser = new SdpParser();
        getServletContext().setAttribute(SdpParser.class.getName(), sdpParser);
    }

    @Override
    protected void doRegister(SipServletRequest register)
            throws ServletException, IOException {
        register.createResponse(SipServletResponse.SC_OK).send();
    }

    @Override
    protected void doInvite(SipServletRequest invite) throws ServletException,
            IOException {
        log("received invite");
        invite.createResponse(SipServletResponse.SC_RINGING).send();
        SdpParser sdpParser = (SdpParser)getServletContext().getAttribute(
                SdpParser.class.getName());
        Charset charset = Charset.forName("UTF-8");

        Object contentObject = invite.getContent();
        String sdp;
        if (contentObject instanceof String)
            sdp = (String)contentObject;
        else
        {
            byte[] content = (byte[])contentObject;
            sdp = new String(content, charset);
        }
        RtpConnection rtpConnection = sdpParser.getRtpConnection(sdp);
        if (rtpConnection == null)
        {
            invite.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE).send();
            return;
        }
        int payloadType;
        String filename;
        List<Integer> payloadTypes = rtpConnection.getPayloadTypes();
        if (payloadTypes.contains(0))
            payloadType = 0;
        else if (payloadTypes.contains(8))
            payloadType = 8;
        else
        {
            invite.createResponse(SipServletResponse.SC_NOT_ACCEPTABLE).send();
            return;
        }
        if (payloadType == 0)
            filename = "StandardGreeting.pcmu.wav";
        else
            filename = "StandardGreeting.pcma.wav";
        URL url = getClass().getClassLoader().getResource(filename);
        File file;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            SipServletResponse resp = invite.createResponse(
                    SipServletResponse.SC_SERVER_INTERNAL_ERROR);
            resp.send();
            return;
        }
        Player player = new Player(file.getAbsolutePath(),
                rtpConnection.getHost(), rtpConnection.getPort(),
                payloadType);
        try {
            player.init();
        } catch (Exception e) {
            SipServletResponse resp = invite.createResponse(
                    SipServletResponse.SC_SERVER_INTERNAL_ERROR);
            resp.send();
            return;
        }
        invite.getApplicationSession(true).setAttribute(
                Player.class.getName(), player);
        SipServletResponse resp =
            invite.createResponse(SipServletResponse.SC_OK);
        String sdpAnswer = "v=0\r\n"
            + "o=user1 123 456 IN IP4 127.0.0.1\r\n"
            + "s=-\r\n"
            + "c=IN IP4 127.0.0.1\r\n"
            + "t=0 0\r\n"
            + "m=audio 6000 RTP/AVP " + payloadType + "\r\n"
            + "a=rtpmap:0 PCMU/8000\r\n";
        
        resp.setContent(sdpAnswer.getBytes(charset), "application/sdp");
        resp.send();
    }

    @Override
    protected void doAck(SipServletRequest ack) throws ServletException,
            IOException {
        SipApplicationSession sipApplicationSession =
            ack.getApplicationSession();
        Player player = (Player)sipApplicationSession.getAttribute(
                Player.class.getName());
        player.play();
    }

    @Override
    protected void doBye(SipServletRequest bye) throws ServletException,
            IOException {
        bye.createResponse(SipServletResponse.SC_OK).send();
        SipApplicationSession sipApplicationSession =
            bye.getApplicationSession();
        Player player = (Player)sipApplicationSession.getAttribute(
                Player.class.getName());
        player.stop();
        bye.getSession().invalidate();
        sipApplicationSession.invalidate();
    }

}
