/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.util.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

@Slf4j
public class ConfigReader {

    private static final char[] PASSWORD = "password".toCharArray(); // NOSONAR

    private static volatile EnvironmentConfiguration instance;

    public static EnvironmentConfiguration environmentConfiguration() {
        if (instance == null) {
            synchronized (ConfigReader.class) {
                if (instance == null) {
                    final String configFileName = "environment-configuration.yml";
                    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
                    File configFile = new File(Objects.requireNonNull(classLoader.getResource(configFileName)).getFile());
                    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                    EnvironmentConfiguration configuration;
                    try {
                        configuration = mapper.readValue(configFile, EnvironmentConfiguration.class);
                    } catch (IOException e) {
                        log.warn("Can't read service configuration from resource file, using default: http://localhost:10010", e);
                        Credentials credentials = new Credentials("user", "user");
                        GatewayServiceConfiguration gatewayServiceConfiguration
                            = new GatewayServiceConfiguration("https", "localhost", 10010, 10017, 1, "10010");
                        DiscoveryServiceConfiguration discoveryServiceConfiguration = new DiscoveryServiceConfiguration("https", "eureka", "password", "localhost", 10011, 1);
                        DiscoverableClientConfiguration discoverableClientConfiguration = new DiscoverableClientConfiguration("ZOWEAPPL");

                        TlsConfiguration tlsConfiguration = TlsConfiguration.builder()
                            .keyAlias("localhost")
                            .keyPassword(PASSWORD)
                            .keyStoreType("PKCS12")
                            .keyStore("../keystore/localhost/localhost.keystore.p12")
                            .keyStorePassword(PASSWORD)
                            .trustStoreType("PKCS12")
                            .trustStore("../keystore/localhost/localhost.truststore.p12")
                            .trustStorePassword(PASSWORD)
                            .build();

                        AuxiliaryUserList auxiliaryUserList = new AuxiliaryUserList("user,password");

                        ZosmfServiceConfiguration zosmfServiceConfiguration = new ZosmfServiceConfiguration("https", "zosmf.acme.com", 1443, "zosmf");

                        configuration = new EnvironmentConfiguration(
                            credentials,
                            gatewayServiceConfiguration,
                            discoveryServiceConfiguration,
                            discoverableClientConfiguration,
                            tlsConfiguration,
                            zosmfServiceConfiguration,
                            auxiliaryUserList,
                            null);
                    }

                    configuration.getCredentials().setUser(System.getProperty("credentials.user", configuration.getCredentials().getUser()));
                    configuration.getCredentials().setPassword(System.getProperty("credentials.password", configuration.getCredentials().getPassword()));

                    configuration.getGatewayServiceConfiguration().setScheme(System.getProperty("gateway.scheme", configuration.getGatewayServiceConfiguration().getScheme()));
                    configuration.getGatewayServiceConfiguration().setHost(System.getProperty("gateway.host", configuration.getGatewayServiceConfiguration().getHost()));
                    configuration.getGatewayServiceConfiguration().setPort(Integer.parseInt(System.getProperty("gateway.port", String.valueOf(configuration.getGatewayServiceConfiguration().getPort()))));
                    configuration.getGatewayServiceConfiguration().setExternalPort(Integer.parseInt(System.getProperty("gateway.externalPort", String.valueOf(configuration.getGatewayServiceConfiguration().getExternalPort()))));
                    configuration.getGatewayServiceConfiguration().setInstances(Integer.parseInt(System.getProperty("gateway.instances", String.valueOf(configuration.getGatewayServiceConfiguration().getInstances()))));
                    configuration.getGatewayServiceConfiguration().setInternalPorts(System.getProperty("gateway.internalPorts", String.valueOf(configuration.getGatewayServiceConfiguration().getInternalPorts())));

                    configuration.getDiscoveryServiceConfiguration().setScheme(System.getProperty("discovery.scheme", configuration.getDiscoveryServiceConfiguration().getScheme()));
                    configuration.getDiscoveryServiceConfiguration().setUser(System.getProperty("discovery.user", configuration.getDiscoveryServiceConfiguration().getUser()));
                    configuration.getDiscoveryServiceConfiguration().setPassword(System.getProperty("discovery.password", configuration.getDiscoveryServiceConfiguration().getPassword()));
                    configuration.getDiscoveryServiceConfiguration().setHost(System.getProperty("discovery.host", configuration.getDiscoveryServiceConfiguration().getHost()));
                    configuration.getDiscoveryServiceConfiguration().setPort(Integer.parseInt(System.getProperty("discovery.port", String.valueOf(configuration.getDiscoveryServiceConfiguration().getPort()))));
                    configuration.getDiscoveryServiceConfiguration().setInstances(Integer.parseInt(System.getProperty("discovery.instances", String.valueOf(configuration.getDiscoveryServiceConfiguration().getInstances()))));

                    configuration.getAuxiliaryUserList().setValue(System.getProperty("auxiliaryUserList.value", String.valueOf(configuration.getAuxiliaryUserList().getValue())));

                    setZosmfConfigurationFromSystemProperties(configuration);
                    setTlsConfigurationFromSystemProperties(configuration);

                    instance = configuration;
                }
            }
        }

        return instance;
    }

    private static void setZosmfConfigurationFromSystemProperties(EnvironmentConfiguration configuration) {
        ZosmfServiceConfiguration zosmfConfiguration = configuration.getZosmfServiceConfiguration();
        zosmfConfiguration.setHost(System.getProperty("zosmf.host", zosmfConfiguration.getHost()));
        String port = System.getProperty("zosmf.port", String.valueOf(zosmfConfiguration.getPort()));
        zosmfConfiguration.setPort(Integer.parseInt(port));
        zosmfConfiguration.setScheme(System.getProperty("zosmf.scheme", zosmfConfiguration.getScheme()));
        zosmfConfiguration.setServiceId(System.getProperty("zosmf.serviceId", zosmfConfiguration.getServiceId()));
    }

    private static char[] getSystemPropertyCharArray(String name, char[] defaultValue) {
        String value = System.getProperty(name);
        if (StringUtils.isEmpty(value)) return defaultValue;
        return  value.toCharArray();
    }

    private static void setTlsConfigurationFromSystemProperties(EnvironmentConfiguration configuration) {
        TlsConfiguration tlsConfiguration = configuration.getTlsConfiguration();
        tlsConfiguration.setKeyAlias(System.getProperty("tlsConfiguration.keyAlias", tlsConfiguration.getKeyAlias()));
        tlsConfiguration.setKeyPassword(getSystemPropertyCharArray("tlsConfiguration.keyPassword", tlsConfiguration.getKeyPassword()));
        tlsConfiguration.setKeyStore(System.getProperty("tlsConfiguration.keyStore", tlsConfiguration.getKeyStore()));
        tlsConfiguration.setKeyStoreType(System.getProperty("tlsConfiguration.keyStoreType", tlsConfiguration.getKeyStoreType()));
        tlsConfiguration.setKeyPassword(getSystemPropertyCharArray("tlsConfiguration.keyStorePassword", tlsConfiguration.getKeyStorePassword()));
        tlsConfiguration.setTrustStoreType(System.getProperty("tlsConfiguration.trustStoreType", tlsConfiguration.getTrustStoreType()));
        tlsConfiguration.setTrustStore(System.getProperty("tlsConfiguration.trustStore", tlsConfiguration.getTrustStore()));
        tlsConfiguration.setTrustStorePassword(getSystemPropertyCharArray("tlsConfiguration.trustStorePassword", tlsConfiguration.getTrustStorePassword()));
    }
}
