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
import org.junit.jupiter.api.*;
import org.zowe.apiml.security.common.ticket.TicketRequest;
import org.zowe.apiml.security.common.ticket.TicketResponse;
import org.zowe.apiml.util.categories.EnvironmentCheck;
import org.zowe.apiml.util.config.ConfigReader;
import org.zowe.apiml.util.config.EnvironmentConfiguration;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.is;
import static org.zowe.apiml.gatewayservice.SecurityUtils.gatewayToken;
import static org.zowe.apiml.gatewayservice.SecurityUtils.getConfiguredSslConfig;

@EnvironmentCheck
class ZosmfLoginBreakTest {

    private String username;
    private String password;
    private String zosmfHost;
    private int zosmfPort;
    private String zosmfAuthEndpoint;
    private String zosmfProtectedEndpoint;
    private String zosmfScheme;
    private String ticketEndpoint;
    private String host;
    private int port;

    @BeforeEach
    void setUp() {
        EnvironmentConfiguration config = ConfigReader.environmentConfiguration();
        username = config.getCredentials().getUser();
        password = config.getCredentials().getPassword();
        zosmfHost = config.getZosmfServiceConfiguration().getHost();
        zosmfPort = config.getZosmfServiceConfiguration().getPort();
        zosmfAuthEndpoint = "/zosmf/services/authenticate";
        zosmfProtectedEndpoint = "/zosmf/restfiles/ds?dslevel=sys1.p*";
        zosmfScheme = config.getZosmfServiceConfiguration().getScheme();
        ticketEndpoint = "/api/v1/gateway/auth/ticket";
        host = config.getGatewayServiceConfiguration().getHost();
        port = config.getGatewayServiceConfiguration().getPort();
    }

    @Test
    void breakZosmfWithFastLogins() {
        System.out.println("+++ login with Basic and get basicJWT +++");
        String basicJWT =
            given().auth().preemptive().basic(username, password)
                .header("X-CSRF-ZOSMF-HEADER", "")
                .when()
                .post(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfAuthEndpoint))
                .then()
                .statusCode(is(SC_OK))
                .extract().cookie("jwtToken");

        System.out.println("+++ logout BasicJWT +++");
        given()
            .cookie("jwtToken", basicJWT)
            .header("X-CSRF-ZOSMF-HEADER", "")
            .when().delete(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfAuthEndpoint))
            .then()
            .statusCode(204);

        System.out.println("+++ login with Basic and get basicJWT +++");
        String basicJWT2 =
            given().auth().preemptive().basic(username, password)
                .header("X-CSRF-ZOSMF-HEADER", "")
                .when()
                .post(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfAuthEndpoint))
                .then()
                .statusCode(is(SC_OK))
                .extract().cookie("jwtToken");

        System.out.println("+++ call zOSMF with basicJWT +++");
        given()
            .cookie("jwtToken", basicJWT2)
            .header("X-CSRF-ZOSMF-HEADER", "")
            .when()
            .get(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfProtectedEndpoint))
            .then()
            .statusCode(200);

        System.out.println("Token1: " + basicJWT);
        System.out.println("Token2: " + basicJWT2);
    }


    // preemptive auth added because of authentication endpoint changed behavior in RSU2012

    @Test
    @Disabled
    void breaksZosmfWithPassticket() {

        System.out.println("+++ login with Basic and get basicJWT +++");
        String basicJWT =
            given().auth().preemptive().basic(username, password).log().all()
                .header("X-CSRF-ZOSMF-HEADER", "")
                .when()
                .post(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfAuthEndpoint))
                .then().log().all()
                .statusCode(is(SC_OK))
                .extract().cookie("jwtToken");

        System.out.println("+++ call zOSMF with basicJWT +++");
        given().log().all()
            .cookie("jwtToken", basicJWT)
            .header("X-CSRF-ZOSMF-HEADER", "")
            .when()
            .get(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfProtectedEndpoint))
            .then().log().all()
            .statusCode(200);

        System.out.println("+++ generate PASSTICKET +++");
        RestAssured.config = RestAssured.config().sslConfig(getConfiguredSslConfig());
        String jwt = gatewayToken();
        TicketRequest ticketRequest = new TicketRequest("IZUDFLT");

        TicketResponse ticketResponse = given()
            .contentType(JSON)
            .body(ticketRequest)
            .header("Authorization", "Bearer " + jwt)
            .when()
            .post(String.format("%s://%s:%d%s", zosmfScheme, host, port, ticketEndpoint))
            .then()
            .statusCode(is(SC_OK))
            .extract().body().as(TicketResponse.class);

        System.out.println("+++ login same user with PassTicket +++");
        String passticketJWT =
            given().auth().preemptive().basic(username, ticketResponse.getTicket()).log().all()
                .header("X-CSRF-ZOSMF-HEADER", "zosmf")
                .when().post(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfAuthEndpoint))
                .then().log().all()
                .statusCode(is(SC_OK))
                .extract().cookie("jwtToken");

        System.out.println("+++ call zOSMF with passticketJWT +++");
        given().log().all()
            .cookie("jwtToken", passticketJWT)
            .header("X-CSRF-ZOSMF-HEADER", "")
            .when().get(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfProtectedEndpoint))
            .then().log().all()
            .statusCode(200);

        System.out.println("+++ call zOSMF with basicJWT +++");
        given().log().all()
            .cookie("jwtToken", basicJWT)
            .header("X-CSRF-ZOSMF-HEADER", "")
            .when().get(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfProtectedEndpoint))
            .then().log().all()
            .statusCode(200);

        System.out.println("+++ logout BasicJWT +++");
        given().log().all()
            .cookie("jwtToken", basicJWT)
            .header("X-CSRF-ZOSMF-HEADER", "")
            .when().delete(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfAuthEndpoint))
            .then().log().all()
            .statusCode(204);

        System.out.println("+++ call zOSMF with passticketJWT +++");
        System.out.println("verify that logout on basicJWT does not affect passticketJWT");
        given().log().all()
            .cookie("jwtToken", passticketJWT)
            .header("X-CSRF-ZOSMF-HEADER", "")
            .when().get(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfProtectedEndpoint))
            .then().log().all()
            .statusCode(200);

        System.out.println("+++ login with Basic and get basicJWT2 +++");
        System.out.println("if this is broken, it's the same token as the original JWT");
        String basicJWT2 =
            given().auth().preemptive().basic(username, password).log().all()
                .header("X-CSRF-ZOSMF-HEADER", "")
                .when()
                .post(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfAuthEndpoint))
                .then().log().all()
                .statusCode(is(SC_OK))
                .extract().cookie("jwtToken");

        System.out.println("+++ call zOSMF with basicJWT2 +++");
        given().log().all()
            .cookie("jwtToken", basicJWT2)
            .header("X-CSRF-ZOSMF-HEADER", "")
            .when()
            .get(String.format("%s://%s:%d%s", zosmfScheme, zosmfHost, zosmfPort, zosmfProtectedEndpoint))
            .then().log().all()
            .statusCode(200);
    }
}
