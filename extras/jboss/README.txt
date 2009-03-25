
For full information to build cipango jboss, see http://docs.codehaus.org/display/JETTY/JBoss

in short:

to build:
mvn -Djboss.version=4.x.y -Djboss.home=/path/to/jboss-4.x.y clean install

to install:
   1. delete $JBOSS-HOME/server/default/deploy/jbossweb-tomcat55.sar (or from whichever deploy directory you
      are using)
   2. ensure you have built the Cipango JBoss module in $cipango.home/extras/jboss
   3. copy the $jcipango.home/extras/jboss/target/cipango-CIPANGO-VERSION-jboss-JBOSS-VERSION.sar to your JBoss
      deploy directory (where CIPANGO-VERSION is the version of cipango you are using and JBOSS-VERSION is the
      version of JBoss).




