#!/bin/sh  
#
# Startup script for cipango under *nix systems (it works under NT/cygwin too).
#
# Configuration files
#
# /etc/default/cipango
#   If it exists, this is read at the start of script. It may perform any 
#   sequence of shell commands, like setting relevant environment variables.
#
# $HOME/.cipangorc
#   If it exists, this is read at the start of script. It may perform any 
#   sequence of shell commands, like setting relevant environment variables.
#
# /etc/cipango.conf
#   If found, and no configurations were given on the command line,
#   the file will be used as this script's configuration. 
#   Each line in the file may contain:
#     - A comment denoted by the pound (#) sign as first non-blank character.
#     - The path to a regular file, which will be passed to cipango as a 
#       config.xml file.
#     - The path to a directory. Each *.xml file in the directory will be
#       passed to cipango as a config.xml file.
#
#   The files will be checked for existence before being passed to cipango.
#
# $CIPANGO_HOME/etc/cipango.xml
#   If found, used as this script's configuration file, but only if
#   /etc/cipango.conf was not present. See above.
#   
# Configuration variables
#
# JAVA_HOME  
#   Home of Java installation. 
#
# JAVA
#   Command to invoke Java. If not set, $JAVA_HOME/bin/java will be
#   used.
#
# JAVA_OPTIONS
#   Extra options to pass to the JVM
#
# CIPANGO_HOME
#   Where cipango is installed. If not set, the script will try go
#   guess it by first looking at the invocation path for the script,
#   and then by looking in standard locations as $HOME/opt/cipango
#   and /opt/cipango. The java system property "cipango.home" will be
#   set to this value for use by configure.xml files, f.e.:
#
#    <Arg><SystemProperty name="cipango.home" default="."/>/sipapps/app.sar</Arg>
#
# CIPANGO_CONSOLE
#   Where cipango console output should go. Defaults to first writeable of
#      /dev/console
#      /dev/tty
#
# SIP_HOST
#   Override the default SIP host for cipango servers. If not set then the
#   default value in the xml configuration file will be used. The java
#   system property "sip.host" will be set to this value for use in
#   configure.xml files. For example, the following idiom is widely
#   used in the demo config files to respect this property in Listener
#   configuration elements:
#
#    <Set name="host"><SystemProperty default="127.0.0.1" name="sip.host"/></Set>
#
# SIP_PORT
#   Override the default SIP port for cipango servers. If not set then the
#   default value in the xml configuration file will be used. The java
#   system property "sip.port" will be set to this value for use in
#   configure.xml files. For example, the following idiom is widely
#   used in the demo config files to respect this property in Listener
#   configuration elements:
#
#    <Set name="Port"><SystemProperty name="sip.port" default="5060"/></Set>
#
# JETTY_PORT
#   Override the default HTTP port for cipango servers. If not set then the
#   default value in the xml configuration file will be used. The java
#   system property "jetty.port" will be set to this value for use in
#   configure.xml files. For example, the following idiom is widely
#   used in the demo config files to respect this property in Listener
#   configuration elements:
#
#    <Set name="Port"><SystemProperty name="jetty.port" default="8080"/></Set>
#   Note: that the config file could ignore this property simply by saying:
#
#    <Set name="Port">8080</Set>
#
# CIPANGO_RUN
#   Where the cipango.pid file should be stored. It defaults to the
#   first available of /var/run, /usr/var/run, and /tmp if not set.
#  
# CIPANGO_PID
#   The cipango PID file, defaults to $CIPANGO_RUN/cipango.pid
#   
# CIPANGO_ARGS
#   The default arguments to pass to cipango.
#

usage()
{
    echo "Usage: $0 {start|stop|run|restart|check|supervise} [ CONFIGS ... ] "
    exit 1
}

[ $# -gt 0 ] || usage

TMPJ=/tmp/j$$

##################################################
# Get the action & configs
##################################################

ACTION=$1
shift
ARGS="$*"
CONFIGS=""

##################################################
# Find directory function
##################################################
findDirectory()
{
    OP=$1
    shift
    for L in $* ; do
        [ $OP $L ] || continue 
        echo $L
        break
    done 
}


##################################################
# See if there's a default configuration file
##################################################
if [ -f /etc/default/cipango ] ; then 
  . /etc/default/cipango
fi

##################################################
# See if there's a user-specific configuration file
##################################################
if [ -f $HOME/.cipangorc ] ; then 
  . $HOME/.cipangorc
fi


##################################################
# cipango's hallmark
##################################################
CIPANGO_INSTALL_TRACE_FILE="start.jar"


##################################################
# Try to determine CIPANGO_HOME if not set
##################################################
if [ -z "$CIPANGO_HOME" ] 
then
  CIPANGO_HOME_1=`dirname "$0"`
  CIPANGO_HOME_1=`dirname "$CIPANGO_HOME_1"`
  if [ -f "${CIPANGO_HOME_1}/${CIPANGO_INSTALL_TRACE_FILE}" ] ; 
  then 
     CIPANGO_HOME=${CIPANGO_HOME_1} 
  fi
fi


##################################################
# if no CIPANGO_HOME, search likely locations.
##################################################
if [ "$CIPANGO_HOME" = "" ] ; then
  STANDARD_LOCATIONS="           \
        $HOME                    \
        $HOME/src                \
        ${HOME}/opt/             \
        /opt                     \
        /java                    \
        /usr/share               \
        /usr/share/java          \
        /usr/local               \
        /usr/local/share         \
        /usr/local/share/java    \
        /home                    \
        "
  CIPANGO_DIR_NAMES="              \
        Cipango                    \
        cipango                    \
        Cipango-*                  \
        cipango-*                  \
        "
        
  CIPANGO_HOME=
  for L in $STANDARD_LOCATIONS 
  do
     for N in $CIPANGO_DIR_NAMES 
     do
         if [ -d $L/$N ] && [ -f "$L/${N}/${CIPANGO_INSTALL_TRACE_FILE}" ] ; 
         then 
            CIPANGO_HOME="$L/$N"
            echo "Defaulting CIPANGO_HOME to $CIPANGO_HOME"
         fi
     done
     [ ! -z "$CIPANGO_HOME" ] && break
  done
fi

##################################################
# No CIPANGO_HOME yet? We're out of luck!
##################################################
if [ -z "$CIPANGO_HOME" ] ; then
    echo "** ERROR: CIPANGO_HOME not set, you need to set it or install in a standard location" 
    exit 1
fi

#####################################################
# Check that cipango is where we think it is
#####################################################
if [ ! -r $CIPANGO_HOME/$CIPANGO_INSTALL_TRACE_FILE ] 
then
   echo "** ERROR: Oops! cipango doesn't appear to be installed in $CIPANGO_HOME"
   echo "** ERROR:  $CIPANGO_HOME/$CIPANGO_INSTALL_TRACE_FILE is not readable!"
   exit 1
fi


###########################################################
# Get the list of config.xml files from the command line.
###########################################################
if [ ! -z "$ARGS" ] 
then
  for A in $ARGS 
  do
    if [ -f $A ] 
    then
       CONF="$A" 
    elif [ -f $CIPANGO_HOME/etc/$A ] 
    then
       CONF="$CIPANGO_HOME/etc/$A" 
    elif [ -f ${A}.xml ] 
    then
       CONF="${A}.xml" 
    elif [ -f $CIPANGO_HOME/etc/${A}.xml ] 
    then
       CONF="$CIPANGO_HOME/etc/${A}.xml" 
    else
       echo "** ERROR: Cannot find configuration '$A' specified in the command line." 
       exit 1
    fi
    if [ ! -r $CONF ] 
    then
       echo "** ERROR: Cannot read configuration '$A' specified in the command line." 
       exit 1
    fi
    CONFIGS="$CONFIGS $CONF"
  done
fi


##################################################
# Try to find this script's configuration file,
# but only if no configurations were given on the
# command line.
##################################################
if [ -z "$CIPANGO_CONF" ] 
then
  if [ -f /etc/cipango.conf ]
  then
     CIPANGO_CONF=/etc/cipango.conf
  elif [ -f "${CIPANGO_HOME}/etc/cipango.conf" ]
  then
     CIPANGO_CONF="${CIPANGO_HOME}/etc/cipango.conf"
  fi
fi

##################################################
# Read the configuration file if one exists
##################################################
CONFIG_LINES=
if [ -z "$CONFIGS" ] && [ -f "$CIPANGO_CONF" ] && [ -r "$CIPANGO_CONF" ] 
then
  CONFIG_LINES=`cat $CIPANGO_CONF | grep -v "^[:space:]*#" | tr "\n" " "` 
fi

##################################################
# Get the list of config.xml files from cipango.conf
##################################################
if [ ! -z "${CONFIG_LINES}" ] 
then
  for CONF in ${CONFIG_LINES} 
  do
    if [ ! -r "$CONF" ] 
    then
      echo "** WARNING: Cannot read '$CONF' specified in '$CIPANGO_CONF'" 
    elif [ -f "$CONF" ] 
    then
      # assume it's a configure.xml file
      CONFIGS="$CONFIGS $CONF" 
    elif [ -d "$CONF" ] 
    then
      # assume it's a directory with configure.xml files
      # for example: /etc/cipango.d/
      # sort the files before adding them to the list of CONFIGS
      XML_FILES=`ls ${CONF}/*.xml | sort | tr "\n" " "` 
      for FILE in ${XML_FILES} 
      do
         if [ -r "$FILE" ] && [ -f "$FILE" ] 
         then
            CONFIGS="$CONFIGS $FILE" 
         else
           echo "** WARNING: Cannot read '$FILE' specified in '$CIPANGO_CONF'" 
         fi
      done
    else
      echo "** WARNING: Don''t know what to do with '$CONF' specified in '$CIPANGO_CONF'" 
    fi
  done
fi

#####################################################
# Run the standard server if there's nothing else to run
#####################################################
if [ -z "$CONFIGS" ] 
then
    CONFIGS="${CIPANGO_HOME}/etc/cipango.xml"
fi


#####################################################
# Find a location for the pid file
#####################################################
if [  -z "$CIPANGO_RUN" ] 
then
  CIPANGO_RUN=`findDirectory -w /var/run /usr/var/run /tmp`
fi

#####################################################
# Find a PID for the pid file
#####################################################
if [  -z "$CIPANGO_PID" ] 
then
  CIPANGO_PID="$CIPANGO_RUN/cipango.pid"
fi

#####################################################
# Find a location for the cipango console
#####################################################
if [  -z "$CIPANGO_CONSOLE" ] 
then
  if [ -w /dev/console ]
  then
    CIPANGO_CONSOLE=/dev/console
  else
    CIPANGO_CONSOLE=/dev/tty
  fi
fi


##################################################
# Check for JAVA_HOME
##################################################
if [ -z "$JAVA_HOME" ]
then
    # If a java runtime is not defined, search the following
    # directories for a JVM and sort by version. Use the highest
    # version number.

    # Java search path
    JAVA_LOCATIONS="\
        /usr/bin \
        /usr/local/bin \
        /usr/local/java \
        /usr/local/jdk \
        /usr/local/jre \
        /opt/java \
        /opt/jdk \
        /opt/jre \
    " 
    JAVA_NAMES="java jre kaffe"
    for N in $JAVA_NAMES ; do
        for L in $JAVA_LOCATIONS ; do
            [ -d $L ] || continue 
            find $L -name "$N" ! -type d | grep -v threads | while read J ; do
                [ -x $J ] || continue
                VERSION=`eval $J -version 2>&1`       
                [ $? = 0 ] || continue
                VERSION=`expr "$VERSION" : '.*"\(1.[0-9\.]*\)"'`
                [ "$VERSION" = "" ] && continue
                expr $VERSION \< 1.2 >/dev/null && continue
                echo $VERSION:$J
            done
        done
    done | sort | tail -1 > $TMPJ
    JAVA=`cat $TMPJ | cut -d: -f2`
    JVERSION=`cat $TMPJ | cut -d: -f1`

    JAVA_HOME=`dirname $JAVA`
    while [ ! -z "$JAVA_HOME" -a "$JAVA_HOME" != "/" -a ! -f "$JAVA_HOME/lib/tools.jar" ] ; do
        JAVA_HOME=`dirname $JAVA_HOME`
    done
    [ "$JAVA_HOME" = "" ] && JAVA_HOME=

    echo "Found JAVA=$JAVA in JAVA_HOME=$JAVA_HOME"
fi


##################################################
# Determine which JVM of version >1.2
# Try to use JAVA_HOME
##################################################
if [ "$JAVA" = "" -a "$JAVA_HOME" != "" ]
then
  if [ ! -z "$JAVACMD" ] 
  then
     JAVA="$JAVACMD" 
  else
    [ -x $JAVA_HOME/bin/jre -a ! -d $JAVA_HOME/bin/jre ] && JAVA=$JAVA_HOME/bin/jre
    [ -x $JAVA_HOME/bin/java -a ! -d $JAVA_HOME/bin/java ] && JAVA=$JAVA_HOME/bin/java
  fi
fi

if [ "$JAVA" = "" ]
then
    echo "Cannot find a JRE or JDK. Please set JAVA_HOME to a >=1.2 JRE" 2>&2
    exit 1
fi

JAVA_VERSION=`expr "$($JAVA -version 2>&1 | head -1)" : '.*1\.\([0-9]\)'`

#####################################################
# See if SIP_HOST is defined
#####################################################
if [ "$SIP_HOST" != "" ] 
then
  JAVA_OPTIONS="$JAVA_OPTIONS -Dsip.host=$SIP_HOST"
fi


#####################################################
# See if SIP_PORT is defined
#####################################################
if [ "$SIP_PORT" != "" ] 
then
  JAVA_OPTIONS="$JAVA_OPTIONS -Dsip.port=$SIP_PORT"
fi

#####################################################
# See if JETTY_PORT is defined
#####################################################
if [ "$JETTY_PORT" != "" ] 
then
  JAVA_OPTIONS="$JAVA_OPTIONS -Djetty.port=$JETTY_PORT"
fi

#####################################################
# Are we running on Windows? Could be, with Cygwin/NT.
#####################################################
case "`uname`" in
CYGWIN*) PATH_SEPARATOR=";";;
*) PATH_SEPARATOR=":";;
esac


#####################################################
# Add cipango properties to Java VM options.
#####################################################
JAVA_OPTIONS="$JAVA_OPTIONS -Dcipango.home=$CIPANGO_HOME "

#####################################################
# This is how the cipango server will be started
#####################################################
RUN_CMD="$JAVA $JAVA_OPTIONS -jar $CIPANGO_HOME/start.jar $CIPANGO_ARGS $CONFIGS"

#####################################################
# Comment these out after you're happy with what 
# the script is doing.
#####################################################
#echo "CIPANGO_HOME     =  $CIPANGO_HOME"
#echo "CIPANGO_CONF     =  $CIPANGO_CONF"
#echo "CIPANGO_RUN      =  $CIPANGO_RUN"
#echo "CIPANGO_PID      =  $CIPANGO_PID"
#echo "CIPANGO_CONSOLE  =  $CIPANGO_CONSOLE"
#echo "CIPANGO_ARGS     =  $CIPANGO_ARGS"
#echo "CONFIGS          =  $CONFIGS"
#echo "JAVA_OPTIONS     =  $JAVA_OPTIONS"
#echo "JAVA             =  $JAVA"


##################################################
# Do the action
##################################################
case "$ACTION" in
  start)
        echo "Starting cipango: "

        if [ -f $CIPANGO_PID ]
        then
            # Test for real activity of cipango
            CIPANGO_CURRENT_PID_VALUE=`cat $CIPANGO_PID`
            CIPANGO_RUNNING_NOW=`ps -edf | grep $CIPANGO_CURRENT_PID_VALUE | grep -v grep | wc -l`
            
            if [ $CIPANGO_RUNNING_NOW -gt "0" ]
            then
                echo "Already Running!!"
                exit 1
            fi
        fi

        echo "STARTED cipango `date`" >> $CIPANGO_CONSOLE

        nohup sh -c "exec $RUN_CMD >>$CIPANGO_CONSOLE 2>&1" >/dev/null &
        echo $! > $CIPANGO_PID
        echo "cipango running pid="`cat $CIPANGO_PID`
        ;;

  stop)
        PID=`cat $CIPANGO_PID 2>/dev/null`
        echo "Shutting down cipango: $PID"
        kill $PID 2>/dev/null
        sleep 2
        kill -9 $PID 2>/dev/null
        rm -f $CIPANGO_PID
        echo "STOPPED `date`" >>$CIPANGO_CONSOLE
        ;;

  restart)
        $0 stop $*
        sleep 5
        $0 start $*
        ;;

  supervise)
       #
       # Under control of daemontools supervise monitor which
       # handles restarts and shutdowns via the svc program.
       #
         exec $RUN_CMD
         ;;

  run|demo)
        echo "Running cipango: "

        if [ -f $CIPANGO_PID ]
        then
            # Test for real activity of cipango
            CIPANGO_CURRENT_PID_VALUE=`cat $CIPANGO_PID`
            CIPANGO_RUNNING_NOW=`ps -edf | grep $CIPANGO_CURRENT_PID_VALUE | wc -l`
  
            if [ $CIPANGO_RUNNING_NOW -gt "1" ]
            then
                echo "Already Running!!"
                exit 1
            fi
        fi

        exec $RUN_CMD
        ;;

  check)
        echo "Checking arguments to cipango: "
        echo "CIPANGO_HOME     =  $CIPANGO_HOME"
        echo "CIPANGO_CONF     =  $CIPANGO_CONF"
        echo "CIPANGO_RUN      =  $CIPANGO_RUN"
        echo "CIPANGO_PID      =  $CIPANGO_PID"
        echo "CIPANGO_CONSOLE  =  $CIPANGO_CONSOLE"
        echo "SIP_HOST         =  $SIP_HOST"
        echo "SIP_PORT         =  $SIP_PORT"
        echo "JETTY_PORT       =  $JETTY_PORT"
        echo "CONFIGS          =  $CONFIGS"
        echo "JAVA_OPTIONS     =  $JAVA_OPTIONS"
        echo "JAVA             =  $JAVA"
        echo "CLASSPATH        =  $CLASSPATH"
        echo "RUN_CMD          =  $RUN_CMD"
        echo
        
        # Test for real activity of cipango

        if [ -f $CIPANGO_RUN/cipango.pid ]
        then
            CIPANGO_CURRENT_PID_VALUE=`cat $CIPANGO_PID`
            CIPANGO_RUNNING_NOW=`ps -edf | grep $CIPANGO_CURRENT_PID_VALUE | wc -l`
            if [ $CIPANGO_RUNNING_NOW -gt "0" ]
            then
              echo "cipango running pid=$CIPANGO_CURRENT_PID_VALUE"
              exit 0
            else 
              echo "cipango not running, but has a pid=$CIPANGO_CURRENT_PID_VALUE"
              exit 1
            fi

        fi
        exit 1
        ;;

*)
        usage
    ;;
esac

exit 0


