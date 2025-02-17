/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.gatewayservice;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.zowe.apiml.util.TestWithStartedInstances;
import org.zowe.apiml.util.categories.GatewayTest;
import org.zowe.apiml.util.categories.TestsNotMeantForZowe;
import org.zowe.apiml.util.config.ConfigReader;
import org.zowe.apiml.util.config.GatewayServiceConfiguration;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.is;

@GatewayTest
class EncodedCharactersTest implements TestWithStartedInstances {


    private String scheme;
    private String host;
    private int port;

    @BeforeAll
    static void beforeClass() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    @BeforeEach
    void setUp() {
        GatewayServiceConfiguration serviceConfiguration = ConfigReader.environmentConfiguration().getGatewayServiceConfiguration();
        scheme = serviceConfiguration.getScheme();
        host = serviceConfiguration.getHost();
        port = serviceConfiguration.getPort();
    }

    @Test
    @TestsNotMeantForZowe
    void shouldCallDiscoverableServiceWithEncodedCharacterAndAllow() {
        final String encodedURI = "/api/v1/discoverableclient/wor%2fld/greeting";

        given()
            .urlEncodingEnabled(false)
            .when()
            .get(String.format("%s://%s:%s%s", scheme, host, port, encodedURI))
            .then()
            .body("content", is("Hello, wor/ld!"))
            .statusCode(is(SC_OK));
    }

    @Test
    void shouldCallCatalogServiceWithEncodedCharacterAndReject() {
        final String encodedURI = "/api/v1/apicatalog/gf%2fd/testcall";

        given()
            .contentType(ContentType.JSON)
            .urlEncodingEnabled(false)
            .when()
            .get(String.format("%s://%s:%s%s", scheme, host, port, encodedURI))
            .then()
            .statusCode(is(SC_BAD_REQUEST));
    }

    @Test
    void shouldCallServiceWithGatewayNotAllowingAndReject() {
        final String encodedURI = "/api/v1/apicatalog/gf%2fd/testcall";

        given()
            .contentType(ContentType.JSON)
            .urlEncodingEnabled(false)
            .when()
            .get(String.format("%s://%s:%s%s", scheme, host, port, encodedURI))
            .then()
            .statusCode(is(SC_BAD_REQUEST));
    }

    @Test
    void shouldCallServiceWithGatewayAllowingAndAllow() {
        final String encodedURI = "/api/v1/apicatalog/gf%2fd/testcall";

        given()
            .contentType(ContentType.JSON)
            .urlEncodingEnabled(true)
            .when()
            .get(String.format("%s://%s:%s%s", scheme, host, port, encodedURI))
            .then()
            .statusCode(is(SC_NOT_FOUND)); //Means not a bad request. Route doesn't actually exist so NOT_FOUND is expected
    }

    @Test
    void shouldCallServiceWithGatewayNotSetAndAllow() {
        final String encodedURI = "/api/v1/apicatalog/gf%2fd/testcall";

        given()
            .contentType(ContentType.JSON)
            .when()
            .get(String.format("%s://%s:%s%s", scheme, host, port, encodedURI))
            .then()
            .statusCode(is(SC_NOT_FOUND)); //Means not a bad request. Route doesn't actually exist so NOT_FOUND is expected
    }
}
