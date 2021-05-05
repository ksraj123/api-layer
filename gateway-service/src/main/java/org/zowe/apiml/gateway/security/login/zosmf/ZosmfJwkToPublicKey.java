/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.gateway.security.login.zosmf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

@RequiredArgsConstructor
@Slf4j
public class ZosmfJwkToPublicKey {

    protected final RestTemplate restTemplateWithoutKeystore;
    private final String zosmfJwtBuilderPath;

    /**
     * Write public key that can be used to validate z/OSMF JWT tokens.
     *
     * @param zosmfUrl Base URL of z/OSMF without trailing /
     * @param filename File name of the resulting PEM file
     * @return True when the file has been updated
     * @throws FileNotFoundException when the filename is invalid
     */
    @HystrixCommand
    public boolean updateJwtPublicKeyFile(String zosmfUrl, String filename, String caAlias, String caKeyStore,
                                          String caKeyStoreType, char[] caKeyStorePassword, char[] caKeyPassword) throws FileNotFoundException {
        try {
            String jwkJson = restTemplateWithoutKeystore.getForObject(zosmfUrl + zosmfJwtBuilderPath,
                String.class);
            JwkToPublicKeyConverter converter = new JwkToPublicKeyConverter();
            String pem = converter.convertFirstPublicKeyJwkToPem(jwkJson, caAlias, caKeyStore, caKeyStoreType, caKeyStorePassword, caKeyPassword);
            try (PrintWriter out = new PrintWriter(filename)) {
                out.println(pem);
            }
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Unable to read z/OSMF JWT public key. JWT support might be not configured in z/OSMF: {}", e.getMessage());
            return false;
        }
    }
}
