
Cipango media api example sip application

This demo application acts as an answering machine. It plays an announcement
and record caller's speech. Its aim is to show how to use cipango media module
which provides simple features like announcement playback, recording, etc.

To run googlecode media example application in debug mode in eclipse:

1) create a launch configuration on cipango module

2) add cipango-dar and media modules eclipse projects in this run configuration
   classpath

3) add the following program arguments (ip address must be yours):

  192.168.2.149:5060 -sipapp ../examples/cipango-example-media/target/cipango-example-media.war

4) add the following vm arguments:

  -Djavax.servlet.sip.ar.spi.SipApplicationRouterProvider=org.cipango.dar.DefaultApplicationRouterProvider
