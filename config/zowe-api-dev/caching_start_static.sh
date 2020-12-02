#
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright Contributors to the Zowe Project.
#

java -Xquickstart \
-Dcaching.storage.mode=novsam \
-Dcaching.storage.vsam.name="//'JANDA06.CACHE3'" \
-Dapiml.service.hostname=usilca32.lvn.broadcom.net \
-Dapiml.service.port=7998 \
-Dapiml.service.serviceIpAddress=0.0.0.0 \
-Dserver.ssl.keyAlias=localhost \
-Dserver.ssl.keyPassword=password \
-Dserver.ssl.keyStore=/C/zowe/keystore2/localhost/localhost.keystore.p12 \
-Dserver.ssl.keyStorePassword=password \
-Dserver.ssl.trustStore=/C/zowe/keystore2/localhost/localhost.truststore.p12 \
-Dserver.ssl.trustStorePassword=password \
-Djava.protocol.handler.pkgs=com.ibm.crypto.provider \
-jar caching-service.jar
