/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.caching.service.vsam;

import org.zowe.apiml.caching.config.VsamConfig;
import org.zowe.apiml.caching.model.KeyValue;

import java.io.UnsupportedEncodingException;

public class VsamRecord {

    private final VsamConfig config;

    private String serviceId;

    private VsamKey key;
    private KeyValue keyValue;

    public VsamRecord(VsamConfig config, String serviceId, KeyValue kv) {
        this.config = config;
        this.serviceId = serviceId;
        this.keyValue = kv;
        this.key = new VsamKey(config);
    }

    public byte[] getBytes() throws UnsupportedEncodingException {
        return VsamUtils.padToLength(key.getKey(serviceId, keyValue.getKey()) + keyValue.getValue(), config.getRecordLength())
            .getBytes(config.getEncoding());
    }

    public byte[] getKeyBytes() throws UnsupportedEncodingException {
        return key.getKeyBytes(serviceId, keyValue);
    }

    @Override
    public String toString() {
        return "VsamRecord{" +
            "config=" + config +
            ", serviceId='" + serviceId + '\'' +
            ", key=" + key.getKey(serviceId, keyValue.getKey()) +
            ", keyValue=" + keyValue +
            '}';
    }
}
