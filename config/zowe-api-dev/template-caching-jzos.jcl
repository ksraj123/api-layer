{{#each user.jobcard}}
{{{this}}}
{{/each}}
//*Contact Info: Api Mediation Layer team - Rest In Api
//*   Alternate:
//* Description: Api Mediation testing deployment instance
//*      Co Req: N/A
//*     Est CPU: UNKNOWN
//* Est Elapsed: 1 DAYS
//*    Start Up:
//*        Stop: you can
//*      Cancel: you can
//*Special Info:
//*LAST UPDATED: 2020-03-24
//*      Region:
//*
//JVMPROC PROC ARGS='',
//   JAR='caching-service.jar',
//   LIBRARY='MVSSYS.JAVA64BT.V8R0M0.MAINT.SIEALNKE',
//   VERSION='86',  < JVMLDM version: 8.0 64-bit
//   REGSIZE='0M',  < Execution region size
//   LOGLVL='+T',  < Debug level: +I(inf) +T(trc)
//   LEPARM='RPTOPTS(ON)'  < Language Environment options
//JAVAJVM  EXEC PGM=JVMLDM&VERSION,REGION=&REGSIZE,
//   PARM='&LEPARM/&LOGLVL -jar &JAR &ARGS'
//STEPLIB  DD   DISP=SHR,DSN=&LIBRARY
//VSMDATA  DD   DISP=SHR,DSN=JANDA06.CACHE
//SYSPRINT DD   SYSOUT=*  < System stdout
//SYSOUT   DD   SYSOUT=*,OUTLIM=1000000  < System stderr
//STDOUT   DD   SYSOUT=*,OUTLIM=1000000  < Java System.out
//STDERR   DD   SYSOUT=*,OUTLIM=1000000  < Java System.err
//CEEDUMP  DD   SYSOUT=*
//CEEOPTS  DD   *
ANYHEAP(2M,1M,ANY,FREE)
HEAP(80M,4M,ANY,KEEP)
HEAPPOOLS(ON,8,10,32,10,128,10,256,10,1024,10,2048,10,0,10,0,
10,0,10,0,10,0,10,0,10)
STACK(64K,16K,ANY,KEEP,128K,128K)
STORAGE(NONE,NONE,NONE,0K)
THREADSTACK(OFF,64K,16K,ANY,KEEP,128K,128K)
/*
//SYSUDUMP DD   SYSOUT=4
//ABNLIGNR DD   DUMMY  < Turn off Abend-AID
//CADVSTOP DD   DUMMY  < Turn off CA-SYMDUMP
//FCOPYOFF DD   DUMMY  < Turn off PDSMAN
//NOPDSMFE DD   DUMMY  < Turn off PDSMAN
//VDSBYPAS DD   DUMMY  < Turn off CA-ALLOCATE
//STDENV   DD   *

dir={{{user.zosTargetDir}}}
basePort={{{user.basePort}}}
systemHostname={{{user.systemHostname}}}

export RUNTIME_HOME={{{user.zosTargetDir}}}
export INSTANCE_HOME={{{user.zosTargetDir}}}
export STC_HOME={{{user.zosTargetDir}}}
export JAVA_HOME={{{user.javaHome}}}
SYS_UTL=/usr/sbin

export ZOWE_PREFIX=ZOWEJ
export DISCOVERY_CODE=DS
export CATALOG_CODE=AC
export GATEWAY_CODE=GW
export ZOWE_EXPLORER_HOST={{{user.systemHostname}}}
export ZOWE_IP_ADDRESS=0.0.0.0
export DISCOVERY_PORT=$((basePort+1))
export CATALOG_PORT=$((basePort+2))
export DISCOVERABLECLIENT_PORT=$((basePort+3))
export CACHING_PORT=$((basePort+4))
export GATEWAY_PORT=$basePort
export STATIC_DEF_CONFIG_DIR=$dir/apidef
export VERIFY_CERTIFICATES=true
export KEY_ALIAS=apiml
export KEYSTORE=$dir/keystore/keystore.p12
export KEYSTORE_TYPE=PKCS12
export KEYSTORE_PASSWORD=password
export TRUSTSTORE=$dir/keystore/truststore.p12
export ROOT_DIR=$dir
export WORKSPACE_DIR=$dir
export ZOWE_MANIFEST=$dir/zowe-manifest.json

export PATH="$SYS_UTL":/bin:"${JAVA_HOME}"/bin

LIBPATH=/lib:/usr/lib:"${JAVA_HOME}"/bin
LIBPATH="$LIBPATH":"${JAVA_HOME}"/bin/classic
LIBPATH="$LIBPATH":"${JAVA_HOME}"/bin/j9vm
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/s390/classic
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/s390/default
LIBPATH="$LIBPATH":"${JAVA_HOME}"/lib/s390/j9vm
LIBPATH="${RUNTIME_HOME}/zoslibs":"$LIBPATH"
LIBPATH="${RUNTIME_HOME}":"$LIBPATH"
export LIBPATH="$LIBPATH":

CLASSPATH="${JAVA_HOME}/lib/tools.jar"
CLASSPATH="${CLASSPATH}":/usr/include/java_classes/IRRRacf.jar
R="runtime"

# Set JZOS specific options
# Use this variable to specify encoding for DD STDOUT and STDERR
#export JZOS_OUTPUT_ENCODING=IBM-1047
# Use this variable to prevent JZOS from handling MVS operator commands
#export JZOS_ENABLE_MVS_COMMANDS=false
# Use this variable to supply additional arguments to main
#export JZOS_MAIN_ARGS=""

# Configure JVM options
IJO="-Xms16m -Xmx512m"

IJO="$IJO -Dibm.serversocket.recover=true"
IJO="$IJO -Dfile.encoding=UTF-8"
IJO="$IJO -Djava.io.tmpdir=/tmp"
IJO="$IJO -Xquickstart"

# Application configuration:
IJO="$IJO -Xquickstart"
IJO="$IJO -Dcaching.storage.mode=vsam "
IJO="$IJO -Dapiml.service.hostname=${ZOWE_EXPLORER_HOST} "
IJO="$IJO -Dapiml.service.port=${CACHING_PORT} "
IJO="$IJO -Dapiml.service.serviceIpAddress=${ZOWE_IP_ADDRESS} "
IJO="$IJO -Dserver.ssl.keyAlias=${KEY_ALIAS} "
IJO="$IJO -Dserver.ssl.keyPassword=${KEYSTORE_PASSWORD} "
IJO="$IJO -Dserver.ssl.keyStore=${KEYSTORE} "
IJO="$IJO -Dserver.ssl.keyStorePassword=${KEYSTORE_PASSWORD} "
IJO="$IJO -Dserver.ssl.trustStore=${TRUSTSTORE} "
IJO="$IJO -Dserver.ssl.trustStorePassword=${KEYSTORE_PASSWORD} "
IJO="$IJO -Djava.protocol.handler.pkgs=com.ibm.crypto.provider "

export CLASSPATH="$CLASSPATH"
echo CLASSPATH=${CLASSPATH}
export IBM_JAVA_OPTIONS="$IJO "

export JAVA_DUMP_HEAP=false
export JAVA_PROPAGATE=NO
export IBM_JAVA_ZOS_TDUMP=NO

cd ${STC_HOME}
echo "Working directory: `pwd`"

/*
//         PEND
//JAVA EXEC PROC=JVMPROC
