/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.zaasclient;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.zowe.apiml.util.TestWithStartedInstances;
import org.zowe.apiml.util.categories.GeneralAuthenticationTest;
import org.zowe.apiml.util.categories.NotForMainframeTest;
import org.zowe.apiml.util.config.ConfigReader;
import org.zowe.apiml.util.config.ConfigReaderZaasClient;
import org.zowe.apiml.zaasclient.config.ConfigProperties;
import org.zowe.apiml.zaasclient.exception.ZaasClientErrorCodes;
import org.zowe.apiml.zaasclient.exception.ZaasClientException;
import org.zowe.apiml.zaasclient.exception.ZaasConfigurationException;
import org.zowe.apiml.zaasclient.service.ZaasClient;
import org.zowe.apiml.zaasclient.service.ZaasToken;
import org.zowe.apiml.zaasclient.service.internal.ZaasClientImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;

@GeneralAuthenticationTest
class ZaasClientIntegrationTest implements TestWithStartedInstances {

    private final static String USERNAME = ConfigReader.environmentConfiguration().getCredentials().getUser();
    private final static String PASSWORD = ConfigReader.environmentConfiguration().getCredentials().getPassword();
    private static final String INVALID_USER = "usr";
    private static final String INVALID_PASS = "usr";
    private static final String NULL_USER = null;
    private static final String NULL_PASS = null;
    private static final String EMPTY_USER = "";
    private static final String EMPTY_PASS = "";
    private static final String NULL_AUTH_HEADER = null;
    private static final String EMPTY_AUTH_HEADER = "";
    private static final String EMPTY_STRING = "";

    private final long now = System.currentTimeMillis();
    private final long expirationForExpiredToken = now - 1000;

    ConfigProperties configProperties;
    ZaasClient tokenService;

    private static String getAuthHeader(String userName, String password) {
        String auth = userName + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
            auth.getBytes(StandardCharsets.ISO_8859_1));
        return "Basic " + new String(encodedAuth);
    }

    private String getToken(long now, long expiration, Key jwtSecretKey) {
        return Jwts.builder()
            .setSubject("user")
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(expiration))
            .setIssuer("APIML")
            .setId(UUID.randomUUID().toString())
            .signWith(jwtSecretKey, SignatureAlgorithm.RS256)
            .compact();
    }

    private Key getDummyKey(ConfigProperties configProperties) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        InputStream inputStream;

        KeyStore ks = KeyStore.getInstance(configProperties.getKeyStoreType());

        File keyStoreFile = new File(configProperties.getKeyStorePath());
        inputStream = new FileInputStream(keyStoreFile);
        ks.load(inputStream, configProperties.getKeyStorePassword());

        return ks.getKey("jwtsecret", configProperties.getKeyStorePassword());
    }

    private void assertThatExceptionContainValidCode(ZaasClientException zce, ZaasClientErrorCodes code) {
        ZaasClientErrorCodes producedErrorCode = zce.getErrorCode();
        assertThat(code.getId(), Is.is(producedErrorCode.getId()));
        assertThat(code.getMessage(), Is.is(producedErrorCode.getMessage()));
        assertThat(code.getReturnCode(), Is.is(producedErrorCode.getReturnCode()));
    }

    @BeforeEach
    void setUp() throws Exception {
        configProperties = ConfigReaderZaasClient.getConfigProperties();
        tokenService = new ZaasClientImpl(configProperties);
    }

    @Test
    void givenValidCredentials_whenUserLogsIn_thenValidTokenIsObtained() throws ZaasClientException {
        String token = tokenService.login(USERNAME, PASSWORD);
        assertNotNull(token);
        assertThat(token, is(not(EMPTY_STRING)));
    }

    private static Stream<Arguments> provideInvalidUsernamePassword() {
        return Stream.of(
            Arguments.of(INVALID_USER, PASSWORD, ZaasClientErrorCodes.INVALID_AUTHENTICATION),
            Arguments.of(NULL_USER, PASSWORD, ZaasClientErrorCodes.EMPTY_NULL_USERNAME_PASSWORD),
            Arguments.of(EMPTY_USER, PASSWORD, ZaasClientErrorCodes.EMPTY_NULL_USERNAME_PASSWORD)
        );
    }


    @ParameterizedTest
    @MethodSource("provideInvalidUsernamePassword")
    void giveInvalidCredentials_whenLoginIsRequested_thenProperExceptionIsRaised(String username, String password, ZaasClientErrorCodes expectedCode) {
        ZaasClientException exception = assertThrows(ZaasClientException.class, () -> tokenService.login(username, password));

        assertThatExceptionContainValidCode(exception, expectedCode);
    }

    private static Stream<Arguments> provideInvalidPassword() {
        return Stream.of(
            Arguments.of(USERNAME, INVALID_PASS, ZaasClientErrorCodes.INVALID_AUTHENTICATION),
            Arguments.of(USERNAME, NULL_PASS, ZaasClientErrorCodes.EMPTY_NULL_USERNAME_PASSWORD)
        );
    }

    @NotForMainframeTest
    @ParameterizedTest
    @MethodSource("provideInvalidPassword")
    void giveInvalidPassword_whenLoginIsRequested_thenProperExceptionIsRaised(String username, String password, ZaasClientErrorCodes expectedCode) {
        ZaasClientException exception = assertThrows(ZaasClientException.class, () -> tokenService.login(username, password));

        assertThatExceptionContainValidCode(exception, expectedCode);
    }

    private static Stream<Arguments> provideInvalidAuthHeaders() {
        return Stream.of(
            Arguments.of(getAuthHeader(INVALID_USER, PASSWORD), ZaasClientErrorCodes.INVALID_AUTHENTICATION),
            Arguments.of(getAuthHeader(NULL_USER, PASSWORD), ZaasClientErrorCodes.INVALID_AUTHENTICATION),
            Arguments.of(getAuthHeader(EMPTY_USER, PASSWORD), ZaasClientErrorCodes.EMPTY_NULL_USERNAME_PASSWORD),
            Arguments.of(NULL_AUTH_HEADER, ZaasClientErrorCodes.EMPTY_NULL_AUTHORIZATION_HEADER),
            Arguments.of(EMPTY_AUTH_HEADER, ZaasClientErrorCodes.EMPTY_NULL_AUTHORIZATION_HEADER)
        );
    }


    @ParameterizedTest
    @MethodSource("provideInvalidAuthHeaders")
    void doLoginWithAuthHeaderInvalidUsername(String authHeader, ZaasClientErrorCodes expectedCode) {
        ZaasClientException exception = assertThrows(ZaasClientException.class, () -> tokenService.login(authHeader));

        assertThatExceptionContainValidCode(exception, expectedCode);
    }

    private static Stream<Arguments> provideInvalidPasswordAuthHeaders() {
        return Stream.of(
            Arguments.of(getAuthHeader(USERNAME, INVALID_PASS), ZaasClientErrorCodes.INVALID_AUTHENTICATION),
            Arguments.of(getAuthHeader(USERNAME, NULL_PASS), ZaasClientErrorCodes.INVALID_AUTHENTICATION),
            Arguments.of(getAuthHeader(USERNAME, EMPTY_PASS), ZaasClientErrorCodes.EMPTY_NULL_USERNAME_PASSWORD)
        );
    }

    @NotForMainframeTest
    @ParameterizedTest
    @MethodSource("provideInvalidPasswordAuthHeaders")
    void doLoginWithAuthHeaderInvalidPassword(String authHeader, ZaasClientErrorCodes expectedCode) {
        ZaasClientException exception = assertThrows(ZaasClientException.class, () -> tokenService.login(authHeader));

        assertThatExceptionContainValidCode(exception, expectedCode);
    }

    @Test
    void givenValidCredentials_whenUserLogsIn_thenValidTokenIsReceived() throws ZaasClientException {
        String token = tokenService.login(getAuthHeader(USERNAME, PASSWORD));
        assertNotNull(token);
        assertThat(token, is(not(EMPTY_STRING)));
    }

    @Test
    void givenValidToken_whenQueriedForDetails_thenValidDetailsAreProvided() throws ZaasClientException {
        String token = tokenService.login(USERNAME, PASSWORD);
        ZaasToken zaasToken = tokenService.query(token);
        assertNotNull(zaasToken);
        assertThat(zaasToken.getUserId(), is(USERNAME));
        assertThat(zaasToken.isExpired(), is(Boolean.FALSE));
    }

    @Test
    void givenInvalidToken_whenQueriedForDetails_thenExceptionIsThrown() {
        assertThrows(ZaasClientException.class, () -> {
            String invalidToken = "INVALID_TOKEN";
            tokenService.query(invalidToken);
        });
    }

    @Test
    void givenExpiredToken_whenQueriedForDetails_thenExceptionIsThrown() {
        assertThrows(ZaasClientException.class, () -> {
            String expiredToken = getToken(now, expirationForExpiredToken, getDummyKey(configProperties));
            tokenService.query(expiredToken);
        });
    }

    @Test
    void givenEmptyToken_whenDetailsAboutTheTokenAreRequested_thenTheExceptionIsThrown() {
        assertThrows(ZaasClientException.class, () -> {
            String emptyToken = "";
            tokenService.query(emptyToken);
        });
    }

    @Test
    void givenValidTicket_whenPassTicketIsRequested_thenValidPassTicketIsReturned() throws ZaasClientException, ZaasConfigurationException {
        String token = tokenService.login(USERNAME, PASSWORD);
        String passTicket = tokenService.passTicket(token, "ZOWEAPPL");
        assertNotNull(passTicket);
        assertThat(token, is(not(EMPTY_STRING)));
    }

    @Test
    void givenInvalidToken_whenPassTicketIsRequested_thenExceptionIsThrown() {
        assertThrows(ZaasClientException.class, () -> {
            String invalidToken = "INVALID_TOKEN";
            tokenService.passTicket(invalidToken, "ZOWEAPPL");
        });
    }

    @Test
    void givenEmptyToken_whenPassTicketIsRequested_thenExceptionIsThrown() {
        assertThrows(ZaasClientException.class, () -> {
            String emptyToken = "";
            tokenService.passTicket(emptyToken, "ZOWEAPPL");
        });
    }

    @Test
    void givenValidTokenButInvalidApplicationId_whenPassTicketIsRequested_thenExceptionIsThrown() throws ZaasClientException {
        String token = tokenService.login(USERNAME, PASSWORD);
        assertThrows(ZaasClientException.class, () -> {
            String emptyApplicationId = "";
            tokenService.passTicket(token, emptyApplicationId);
        });
    }

    @Test
    void givenValidTokenBut_whenLogoutIsCalled_thenSuccess() throws ZaasClientException {
        String token = tokenService.login(USERNAME, PASSWORD);
        assertDoesNotThrow(() -> tokenService.logout(token));
    }

    @Test
    void givenInvalidTokenBut_whenLogoutIsCalled_thenExceptionIsThrown() {
        String token = "";
        assertThrows(ZaasClientException.class, () ->
            tokenService.logout(token));
    }
}
