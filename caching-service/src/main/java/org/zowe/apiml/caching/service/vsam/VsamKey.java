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

import lombok.Getter;
import org.zowe.apiml.caching.model.KeyValue;
import org.zowe.apiml.zfile.ZFileConstants;

import java.io.UnsupportedEncodingException;

public class VsamKey {

    protected static final String ENCODING = ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE;

    @Getter
    int keyLength;

    public VsamKey(int keyLength) {
        if (keyLength < 23) {
            throw new IllegalArgumentException("VsamKey cannot have length smaller than 23");
        }
        this.keyLength = keyLength;
    }

    @Override
    public String toString() {
        return "vsamkey";
    }

    public String getKey(String serviceId, String key) {
        return VsamUtils.padToLength(serviceId.hashCode() + ":" + key.hashCode(), keyLength);
    }

    public String getKey(String serviceId, KeyValue keyValue) {
        return VsamUtils.padToLength(serviceId.hashCode() + ":" + keyValue.getKey().hashCode(), keyLength);
    }

    public byte[] getKeyBytes(String serviceId, String key) throws UnsupportedEncodingException {
        return getKey(serviceId, key).getBytes(ENCODING);
    }

    public byte[] getKeyBytes(String serviceId, KeyValue keyValue) throws UnsupportedEncodingException {
        return getKey(serviceId, keyValue.getKey()).getBytes(ENCODING);
    }

    public String getKeySidOnly(String serviceId) {
        return VsamUtils.padToLength(String.valueOf(serviceId.hashCode()), keyLength);
    }

    public byte[] getKeyBytesSidOnly(String serviceId) throws UnsupportedEncodingException {
        return getKeySidOnly(serviceId).getBytes(ENCODING);
    }
}
