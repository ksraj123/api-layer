#!/bin/sh

#
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright Contributors to the Zowe Project.
#

# Variables required on shell:
# - ZOWE_PREFIX
# - DISCOVERY_PORT - the port the discovery service will use
# - CATALOG_PORT - the port the api catalog service will use
# - GATEWAY_PORT - the port the api gateway service will use
# - VERIFY_CERTIFICATES - boolean saying if we accept only verified certificates
# - DISCOVERY_PORT - The port the data sets server will use
# - KEY_ALIAS
# - KEYSTORE - The keystore to use for SSL certificates
# - KEYSTORE_TYPE - The keystore type to use for SSL certificates
# - KEYSTORE_PASSWORD - The password to access the keystore supplied by KEYSTORE
# - KEY_ALIAS - The alias of the key within the keystore
# - ALLOW_SLASHES - Allows encoded slashes on on URLs through gateway
# - ZOWE_MANIFEST - The full path to Zowe's manifest.json file

# API Mediation Layer Debug Mode
# To activate `debug` mode, set LOG_LEVEL=debug (in lowercase)
LOG_LEVEL=diag

# If set append $ZWEAD_EXTERNAL_STATIC_DEF_DIRECTORIES to $STATIC_DEF_CONFIG_DIR
APIML_STATIC_DEF=${STATIC_DEF_CONFIG_DIR}
if [[ ! -z "$ZWEAD_EXTERNAL_STATIC_DEF_DIRECTORIES" ]]
then
  APIML_STATIC_DEF="${APIML_STATIC_DEF};${ZWEAD_EXTERNAL_STATIC_DEF_DIRECTORIES}"
fi
echo "active profile" $LOG_LEVEL
echo "starting " ${DISCOVERY_CODE}

_BPX_JOBNAME=${ZOWE_PREFIX}CS java -Xms32m -Xmx256m -Xquickstart \
    -Dibm.serversocket.recover=true \
    -Dfile.encoding=UTF-8 \
    -Djava.io.tmpdir=/tmp \
    -Dapiml.service.hostname=${ZOWE_EXPLORER_HOST} \
    -Dapiml.service.port=${CACHING_PORT} \
    -Dapiml.service.serviceIpAddress=${ZOWE_IP_ADDRESS} \
    -Dapiml.service.discoveryServiceUrls=https://${ZOWE_EXPLORER_HOST}:${DISCOVERY_PORT}/eureka/ \
    -Dserver.ssl.keyAlias=${KEY_ALIAS} \
    -Dserver.ssl.keyPassword=${KEYSTORE_PASSWORD} \
    -Dserver.ssl.keyStore=${KEYSTORE} \
    -Dserver.ssl.keyStorePassword=${KEYSTORE_PASSWORD} \
    -Dserver.ssl.trustStore=${TRUSTSTORE} \
    -Dserver.ssl.trustStorePassword=${KEYSTORE_PASSWORD} \
    -Djava.protocol.handler.pkgs=com.ibm.crypto.provider \
    -jar ${ROOT_DIR}"/caching-service.jar" &
echo "Done"
