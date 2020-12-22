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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Version implements Comparable<Version> {

    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    private final String[] numbers;
    private final boolean snapshot;

    public Version(String version) {
        this.snapshot = version.endsWith(SNAPSHOT_SUFFIX);
        if (this.snapshot) {
            version = version.substring(0, version.length() - SNAPSHOT_SUFFIX.length());
        }
        this.numbers = version.split("\\.");
    }

    public int getLength() {
        return numbers.length;
    }

    public String getNumber(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("Index of number has to be equal or greater than zero");
        }

        if (i >= numbers.length) {
            return "0";
        }

        return numbers[i];
    }

    private int compare(String a, String b) {
        if (StringUtils.isNumeric(a) && StringUtils.isNumeric(b)) {
            return Integer.valueOf(a).compareTo(Integer.valueOf(b));
        }
        return a.compareTo(b);
    }

    public Version getSubversion(int count) {
        boolean containsSnapshot = count >= numbers.length && this.snapshot;

        if (count > numbers.length) {
            count = numbers.length;
        }

        return new Version(Arrays.copyOf(numbers, count), containsSnapshot);
    }

    @Override
    public int compareTo(Version o) {
        int length = Math.max(getLength(), o.getLength());
        for (int i = 0; i < length; i++) {
            int c = compare(getNumber(i), o.getNumber(i));
            if (c != 0) {
                return c;
            }
        }

        if (this.snapshot != o.snapshot) {
            return this.snapshot ? -1 : 1;
        }

        return 0;
    }

    @Override
    public String toString() {
        String version = StringUtils.join(numbers, '.');
        if (snapshot) {
            return version + SNAPSHOT_SUFFIX;
        }
        return version;
    }

}
