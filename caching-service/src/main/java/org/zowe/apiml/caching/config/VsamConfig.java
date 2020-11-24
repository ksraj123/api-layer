/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.caching.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class VsamConfig {

    @Value("${caching.storage.vsam.name://CACHE}")
    private String vsamFileName;
    @Value("${caching.storage.vsam.keyLength:32}")
    private int vsamKeyLength;
    @Value("${caching.storage.vsam.recordLength:512}")
    private int vsamRecordLength;

}
