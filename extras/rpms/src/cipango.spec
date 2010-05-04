#
# Spec file for cipango..
#
%define _topdir	 	%(echo $HOME)/rpm
%define buildroot %{_topdir}/%{name}-%{version}-buildroot

BuildRoot:	%{buildroot}
Summary: A Java container of Sip Servlet applications
Name: cipango
Version: @@@VERSION@@@
Release: SNAPSHOT
License: Apache 2
Group: Applications/SIP
Source: http://cipango.googlecode.com/files/cipango-%{version}-src.zip
URL: http://confluence.cipango.org/
Distribution: CentOS/RedHat 
Vendor: Nexcom Systems
Packager: Olivier Le Bihan <olivier.lebihan@nexcom.fr>

BuildArch:      noarch
BuildRequires:  java-devel >= 1.5.0
BuildRequires:  jpackage-utils >= 0:1.7.2
BuildRequires:  ant >= 0:1.6
BuildRequires:  subversion >= 0:1.4.0
BuildRequires:  maven2 >= 2.0.4-10jpp

Requires: jetty6
Provides: cipango

%description
Cipango is an open-source SIP/HTTP java container.
It is based on SipServlet 1.1 (JSR 289).


%prep
%setup
#mettre au format tar.gz et pas zip pour que le setup fonctionne 
# http://www.rpm.org/max-rpm/s1-rpm-inside-macros.html

%build
%install 

mkdir -p $RPM_BUILD_ROOT/etc
mkdir -p $RPM_BUILD_ROOT/etc/default
mkdir -p $RPM_BUILD_ROOT/etc/init.d
mkdir -p $RPM_BUILD_ROOT/usr
mkdir -p $RPM_BUILD_ROOT/usr/share
mkdir -p $RPM_BUILD_ROOT/usr/share/jetty6
mkdir -p $RPM_BUILD_ROOT/usr/share/jetty6/etc
mkdir -p $RPM_BUILD_ROOT/usr/share/jetty6/lib
mkdir -p $RPM_BUILD_ROOT/usr/share/jetty6/lib/annotations
mkdir -p $RPM_BUILD_ROOT/usr/share/jetty6/lib/plus
mkdir -p $RPM_BUILD_ROOT/usr/share/jetty6/lib/ext
mkdir -p $RPM_BUILD_ROOT/usr/share/jetty6/lib/management
cp   %{_topdir}/SOURCES/jetty.conf $RPM_BUILD_ROOT/etc/
cp   %{_topdir}/SOURCES/jetty $RPM_BUILD_ROOT/etc/default/
cp   %{_topdir}/SOURCES/start.config.cipango $RPM_BUILD_ROOT/usr/share/jetty6/etc
mvn -Djetty.home=$RPM_BUILD_ROOT/usr/share/jetty6 clean install


%files
/usr/share/jetty6/README-CIPANGO.txt
/usr/share/jetty6/VERSION-CIPANGO.txt
%config /etc/jetty.conf
%config /etc/default/jetty
%config /usr/share/jetty6/etc/start.config.cipango
%config /usr/share/jetty6/etc/cipango-annot.xml
%config /usr/share/jetty6/etc/cipango-jmx.xml
%config /usr/share/jetty6/etc/cipango-tls.xml
%config /usr/share/jetty6/etc/cipango-plus.xml
%config /usr/share/jetty6/etc/cipango.xml
%config /usr/share/jetty6/etc/dar.properties
%config /usr/share/jetty6/etc/diameter.xml
%config /usr/share/jetty6/etc/sipdefault.xml
/usr/share/jetty6/lib/annotations/asm-3.1.jar
/usr/share/jetty6/lib/annotations/asm-commons-3.1.jar
/usr/share/jetty6/lib/annotations/cipango-annotations-%{version}.jar
/usr/share/jetty6/lib/cipango-%{version}.jar
/usr/share/jetty6/lib/ext/cipango-dar-%{version}.jar
/usr/share/jetty6/lib/ext/cipango-diameter-%{version}.jar
/usr/share/jetty6/lib/ext/stax-api-1.0.1.jar
/usr/share/jetty6/lib/ext/xmlbeans-2.4.0.jar
/usr/share/jetty6/lib/management/cipango-management-%{version}.jar
/usr/share/jetty6/lib/plus/cipango-plus-%{version}.jar
/usr/share/jetty6/lib/sip-api-1.1.jar
/usr/share/jetty6/sipapps/test/WEB-INF/classes/org/cipango/example/Binding.class
/usr/share/jetty6/sipapps/test/WEB-INF/classes/org/cipango/example/Call.class
/usr/share/jetty6/sipapps/test/WEB-INF/classes/org/cipango/example/ClickToDialHttpServlet.class
/usr/share/jetty6/sipapps/test/WEB-INF/classes/org/cipango/example/ClickToDialService.class
/usr/share/jetty6/sipapps/test/WEB-INF/classes/org/cipango/example/ClickToDialSipServlet$CancelCallTask.class
/usr/share/jetty6/sipapps/test/WEB-INF/classes/org/cipango/example/ClickToDialSipServlet.class
/usr/share/jetty6/sipapps/test/WEB-INF/classes/org/cipango/example/OamServlet.class
/usr/share/jetty6/sipapps/test/WEB-INF/classes/org/cipango/example/ProxyRegistrarServlet$BindingScavenger.class
/usr/share/jetty6/sipapps/test/WEB-INF/classes/org/cipango/example/ProxyRegistrarServlet.class
/usr/share/jetty6/sipapps/test/WEB-INF/sip.xml
/usr/share/jetty6/sipapps/test/WEB-INF/web.xml
/usr/share/jetty6/sipapps/test/index.html

%clean
rm -fr %{_topdir}/%{name}-%{version}-buildroot
rm -fr %{_topdir}/BUILD/%{name}-%{version}
