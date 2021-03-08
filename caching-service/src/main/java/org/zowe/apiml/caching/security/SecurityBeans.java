/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.caching.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityBeans {

    @Bean
    public ApprovedCertificateList getApprovedCertificateList() {
        return ApprovedCertificateList.of("CN=APIMTST,OU=CA CZ,O=Broadcom,L=Prague,ST=Czechia,C=CZ");
    }
}
