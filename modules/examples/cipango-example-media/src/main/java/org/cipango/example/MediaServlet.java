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


package org.cipango.example;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.cipango.media.Player;
import org.cipango.media.PlayerListener;
import org.cipango.media.Recorder;

public class MediaServlet extends SipServlet implements PlayerListener
{

    private static final long serialVersionUID = 1L;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        log("init");
        SdpParser sdpParser = new SdpParser();
        ServletContext servletContext = getServletContext();
        servletContext.setAttribute(SdpParser.class.getName(), sdpParser);
        Map<Integer, Recorder> recorders = new HashMap<Integer, Recorder>();
        servletContext.setAttribute(Recorder.class.getName(), recorders);
    }

    @Override
    protected void doRegister(SipServletRequest register)
            throws ServletException, IOException {
        log("doRegister");
        register.createResponse(SipServletResponse.SC_OK).send();
    }

    @Override
    protected void doInvite(SipServletRequest invite) throws ServletException,
            IOException {
        log("doInvite");
        invite.createResponse(SipServletResponse.SC_RINGING).send();
        ServletContext servletContext = getServletContext();
        SdpParser sdpParser = (SdpParser)servletContext.getAttribute(
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
            filename = "AnsweringMachine.pcmu.wav";
        else
            filename = "AnsweringMachine.pcma.wav";
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
        player.addEventListener(this);
        try {
            player.init();
        } catch (Exception e) {
            SipServletResponse resp = invite.createResponse(
                    SipServletResponse.SC_SERVER_INTERNAL_ERROR);
            resp.send();
            return;
        }
        int localPort = player.getLocalPort();
        Recorder recorder = new Recorder(localPort);
        Map<Integer, Recorder> recorders = (Map<Integer, Recorder>)
            servletContext.getAttribute(Recorder.class.getName());
        recorders.put(localPort, recorder);
        invite.getApplicationSession(true).setAttribute(
                Player.class.getName(), player);
        SipServletResponse resp =
            invite.createResponse(SipServletResponse.SC_OK);
        String sdpAnswer = "v=0\r\n"
            + "o=user1 123 456 IN IP4 127.0.0.1\r\n"
            + "s=-\r\n"
            + "c=IN IP4 127.0.0.1\r\n"
            + "t=0 0\r\n"
            + "m=audio " + player.getLocalPort() + " RTP/AVP "
                + payloadType + "\r\n"
            + "a=rtpmap:0 PCMU/8000\r\n";
        
        resp.setContent(sdpAnswer.getBytes(charset), "application/sdp");
        resp.send();
    }

    @Override
    protected void doAck(SipServletRequest ack) throws ServletException,
            IOException {
        log("doAck");
        SipApplicationSession sipApplicationSession =
            ack.getApplicationSession();
        Player player = (Player)sipApplicationSession.getAttribute(
                Player.class.getName());
        player.play();
    }

    @Override
    protected void doBye(SipServletRequest bye) throws ServletException,
            IOException {
        log("doBye");
        bye.createResponse(SipServletResponse.SC_OK).send();
        SipApplicationSession sipApplicationSession =
            bye.getApplicationSession();
        Player player = (Player)sipApplicationSession.getAttribute(
                Player.class.getName());
        player.stop();
        ServletContext servletContext = getServletContext();
        Map<Integer, Recorder> recorders = (Map<Integer, Recorder>)
            servletContext.getAttribute(Recorder.class.getName());
        Recorder recorder = recorders.get(player.getLocalPort());
        recorder.stop();
        recorders.remove(player.getLocalPort());
        bye.getSession().invalidate();
        sipApplicationSession.invalidate();
    }

    public void endOfFile(Player player) {
        log("endOfFile");
        ServletContext servletContext = getServletContext();
        Map<Integer, Recorder> recorders = (Map<Integer, Recorder>)
            servletContext.getAttribute(Recorder.class.getName());
        Recorder recorder = recorders.get(player.getLocalPort());
        recorder.init();
        recorder.record();
    }

}
