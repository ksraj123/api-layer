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

import org.junit.jupiter.api.Test;
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

    CertificateHeaderFilter underTest = new CertificateHeaderFilter();
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    MockFilterChain filterChain = new MockFilterChain();

    @Test
    void callsProceedFilterChainAtLeastOnce() throws ServletException, IOException {
        MockFilterChain spyChain = spy(filterChain);
        underTest.doFilterInternal(request, response, spyChain);
        verify(spyChain,times(1)).doFilter(request, response);
    }

    @Test
    void authorizesEveryRequest() throws ServletException, IOException {
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        underTest.doFilterInternal(request, response, filterChain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated(), is(true));
    }
}
