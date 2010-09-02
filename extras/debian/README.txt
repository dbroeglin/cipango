BUILD DEPENDENCIES:
 cdbs
 debhelper
 devscripts
 fakeroot
 
 JAVA DEBS:
 ant
 maven

------------------------------------------------------------------
BEFORE GENERATING DEB BINARIES

 You need to build Cipango in a specific $jetty.home repository.
 $jetty.home is a maven property configured via the "settings.xml" file

-------------------------------------------------------------------

CLEAN:
$ mvn clean

GENERATING SOURCE and BINARIES:
$ mvn install

MANUAL INSTALLATION:
$ sudo dpkg -i cipango_1.0.0pre1-1_i386.deb

-------------------------------------------------------------------

GENERATED DEB SOURCES:
 cipango-1.0.0pre1.orig.tar.gz
 cipango_1.0.0pre1-1.dsc
 cipango_1.0.0pre1-1.diff.gz


GENERATED DEB BINARIES:
 cipango
  - The sip servlet extension of Jetty6 (depends: jetty6 http://docs.codehaus.org/display/JETTY/Debian+Packages)
  - includes:
     scripts, config files, docs
