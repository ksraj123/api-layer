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

import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class CertificateHeaderFilterTest {

    ApprovedCertificateList approvedCertificateList = ApprovedCertificateList.of("cert1", "authorizeme");
    CertificateHeaderFilter underTest = new CertificateHeaderFilter(approvedCertificateList);
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    @Test
    void callsProceedFilterChainAtLeastOnce() throws ServletException, IOException {
        MockFilterChain spyChain = spy(filterChain);
        underTest.doFilterInternal(request, response, spyChain);
        verify(spyChain,times(1)).doFilter(request, response);
    }

    @Nested
    class WhenCalledWithHeader{

        @BeforeEach
        void clearContext() {
            SecurityContextHolder.clearContext();
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        void noAuthorizationWithoutHeader() throws ServletException, IOException {
            underTest.doFilterInternal(request, response, filterChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        void noAuthorizationWithAnyHeader() throws ServletException, IOException {
            request.addHeader("myHeader", "myValue");
            underTest.doFilterInternal(request, response, filterChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        void noAuthorizationWithEmptyHeader() throws ServletException, IOException {
            request.addHeader(CertificateHeaderFilter.CERT_HEADER_NAME, "");
            underTest.doFilterInternal(request, response, filterChain);
            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        void authorizationWithCorrectValue() throws ServletException, IOException {
            request.addHeader(CertificateHeaderFilter.CERT_HEADER_NAME, "authorizeme");
            underTest.doFilterInternal(request, response, filterChain);
            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated(), is(true));
        }

    }

}
