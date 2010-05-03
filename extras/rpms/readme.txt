* Building The Source RPM

- Build The source rpm (at this point it is packed as a tar file), execute the command below to generate the source rpm
mvn clean install

- the command  above produces a tar file target/cipango-source-rpm.tar
- extract the tar file ( target/cipango-source-rpm.tar ) at your home directory using a none root account

tar -xzvf target/cipango-source-rpm.tar.gz -C ~



* Building The Binary RPMs

- at your home directory, execute the command below, you may need to install a list of required see `Installing Required Packages`
rpmbuild -ba rpm/SPECS/cipango.spec



* Installing Required Packages

The build process requires several packages to be installed from
jpackage.  consult the jpackage documentation to install:

  java-devel >= 1.5.0
  jpackage-utils >= 0:1.7.2
  ant >= 0:1.6
  maven2 >= 2.0.4-10jpp (You may have dependency problems to install maven2. Download the last version from http://maven.apache.org/ and install it.)

* Installing the rpm
cd rpm/RPMS/noarch/
rpm -ivh cipango-${cipango-version}.noarch.rpm

