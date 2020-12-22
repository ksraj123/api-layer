/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.product.version;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class VersionTest {

    @ParameterizedTest
    @CsvSource(delimiter = ',', value = {
        "3,1.2.1",
        "2,1.2",
        "1,1",
        "1,1..",
        "3,1..1",
        "3,3.2.1-SNAPSHOT"
    })
    void testGetLength_whenVersionCreated_thenReturnByDotCount(int count, String version) {
        assertEquals(count, new Version(version).getLength());
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', value = {
        "1.2.3,2,1.2",
        "1.2.3,3,1.2.3",
        "1.2.3,4,1.2.3",
        "2.5-SNAPSHOT,3,2.5-SNAPSHOT",
        "2.5-SNAPSHOT,1,2"
    })
    void testVersion_whenGetSubversion_thenCutRightPart(String source, int length, String target) {
        assertEquals(new Version(target), new Version(source).getSubversion(length));
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', value = {
        "3.4,2.9",
        "1.2,1.1",
        "3.5.6,3.5.5",
        "3.4,3.4-SNAPSHOT",
        "3.4b,3.4a"
    })
    void testComparison_whenTwoVersions_thenFirstIsGreater(String greater, String smaller) {
        assertTrue(new Version(greater).compareTo(new Version(smaller)) > 0);
    }

    @ParameterizedTest
    @CsvSource(delimiter = ',', value = {
        "3.4,3.4",
        "1.01,1.1",
        "07.07,7.7",
        "1.2-SNAPSHOT,1.2-SNAPSHOT",
        "1.3A,1.3A"
    })
    void testComparison_whenTwoVersions_thenAreSame(String a, String b) {
        assertEquals(0, new Version(a).compareTo(new Version(b)));
    }

    @Test
    void testGetNumber_whenNegativeIndexSet_thenException() {
        Version version = new Version("1.2");
        assertThrows(IllegalArgumentException.class, () -> version.getNumber(-1));
    }

    @Test
    void testGetNumber_whenPositiveIndex_thenReturn() {
        Version version = new Version("3.5.6A-SNAPSHOT");
        assertEquals("3", version.getNumber(0));
        assertEquals("5", version.getNumber(1));
        assertEquals("6A", version.getNumber(2));
        assertEquals("0", version.getNumber(3));
        assertEquals("0", version.getNumber(1000));
    }

    @Test
    void testToString() {
        assertEquals("1.5.6a-SNAPSHOT", new Version("1.5.6a-SNAPSHOT").toString());
        assertEquals("3.02", new Version("3.02").toString());
    }

}
