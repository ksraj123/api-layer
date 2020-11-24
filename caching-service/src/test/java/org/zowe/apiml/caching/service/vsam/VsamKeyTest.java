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

import org.junit.jupiter.api.Test;
import org.zowe.apiml.caching.model.KeyValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VsamKeyTest {

    @Test
    void canGetInformationAboutKey() {
        int keyLength = 30;
        VsamKey underTest = new VsamKey(keyLength);
        assertThat(underTest.getKeyLength(), is(30));
    }

    @Test
    void canGetKey() {
        int keyLength = 30;
        VsamKey underTest = new VsamKey(keyLength);

        String serviceId = "gateway";
        String key = "apiml.service.name";
        assertThat(underTest.getKey(serviceId, key).length(), is(keyLength));
        assertThat(underTest.getKey(serviceId, key), containsString(String.valueOf(serviceId.hashCode())));
        assertThat(underTest.getKey(serviceId, key), containsString(":"));
        assertThat(underTest.getKey(serviceId, key), containsString(String.valueOf(key.hashCode())));
    }

    @Test
    void canGetKeyWithJustTheSid() {
        int keyLength = 30;
        VsamKey underTest = new VsamKey(keyLength);

        String serviceId = "gateway";
        String key = "apiml.service.name";
        assertThat(underTest.getKeySidOnly(serviceId).length(), is(keyLength));
        assertThat(underTest.getKeySidOnly(serviceId), containsString(String.valueOf(serviceId.hashCode())));
        assertThat(underTest.getKeySidOnly(serviceId), not(containsString(":")));
        assertThat(underTest.getKeySidOnly(serviceId), not(containsString(String.valueOf(key.hashCode()))));

    }

    @Test
    void canGetKeyFromKeyValue() {
        KeyValue kv = new KeyValue("key", "value");
        String serviceId = "serviceId";
        int keyLength = 30;
        VsamKey underTest = new VsamKey(keyLength);

        assertThat(underTest.getKey(serviceId, kv), notNullValue());
    }

    @Test
    void hasMinimalLength() {
        assertThrows(IllegalArgumentException.class, () -> new VsamKey(22));
        assertDoesNotThrow(() -> new VsamKey(23));
    }


}
