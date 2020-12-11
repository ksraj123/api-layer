#
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright Contributors to the Zowe Project.
#

./gradlew caching-service::build
# zowe-api-dev stop
zowe-api-dev config --name caching
# zowe-api-dev start --job
zowe zos-uss issue ssh "chmod +x start.sh" --cwd "~/../../z/masserv/janda06/zoweapidev/"
zowe zos-uss issue ssh "./start.sh" --cwd "~/../../z/masserv/janda06/zoweapidev/"
