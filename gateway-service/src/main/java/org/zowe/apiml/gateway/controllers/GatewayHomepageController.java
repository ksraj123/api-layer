/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.gateway.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.zowe.apiml.gateway.security.login.Providers;
import org.zowe.apiml.product.version.BuildInfo;
import org.zowe.apiml.product.version.BuildInfoDetails;

import java.util.List;
import java.util.Map;

import static org.zowe.apiml.constants.EurekaMetadataDefinition.*;

/**
 * Main page for Gateway, displaying status of Apiml services and build version information
 */
@Controller
public class GatewayHomepageController {

    private static final String SUCCESS_ICON_NAME = "success";

    private final DiscoveryClient discoveryClient;
    private final Providers providers;

    private BuildInfo buildInfo;
    private String buildString;

    private String apiCatalogServiceId;

    @Autowired
    public GatewayHomepageController(DiscoveryClient discoveryClient,
                                     Providers providers,
                                     @Value("${apiml.catalog.serviceId:}") String apiCatalogServiceId) {
        this(discoveryClient, providers, new BuildInfo(), apiCatalogServiceId);
    }

    public GatewayHomepageController(DiscoveryClient discoveryClient,
                                     Providers providers,
                                     BuildInfo buildInfo,
                                     String apiCatalogServiceId) {
        this.discoveryClient = discoveryClient;
        this.providers = providers;
        this.buildInfo = buildInfo;
        this.apiCatalogServiceId = apiCatalogServiceId;

        initializeBuildInfos();
    }

    @GetMapping("/")
    public String home(Model model) {
        initializeCatalogAttributes(model);
        initializeMetricsAttributes(model);
        initializeDiscoveryAttributes(model);
        initializeAuthenticationAttributes(model);

        model.addAttribute("buildInfoText", buildString);
        return "home";
    }

    private void initializeBuildInfos() {
        BuildInfoDetails buildInfoDetails = buildInfo.getBuildInfoDetails();
        buildString = "Build information is not available";
        if (!buildInfoDetails.getVersion().equalsIgnoreCase("unknown")) {
            buildString = String.format("Version %s build # %s", buildInfoDetails.getVersion(), buildInfoDetails.getNumber());
        }
    }

    private void initializeDiscoveryAttributes(Model model) {
        String discoveryStatusText = null;
        String discoveryIconName = null;

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("discovery");
        if (serviceInstances != null) {
            int discoveryCount = serviceInstances.size();
            switch (discoveryCount) {
                case 0:
                    discoveryStatusText = "The Discovery Service is not running";
                    discoveryIconName = "danger";
                    break;
                case 1:
                    discoveryStatusText = "The Discovery Service is running";
                    discoveryIconName = SUCCESS_ICON_NAME;
                    break;
                default:
                    discoveryStatusText = discoveryCount + " Discovery Service instances are running";
                    discoveryIconName = SUCCESS_ICON_NAME;
                    break;
            }
        }

        model.addAttribute("discoveryStatusText", discoveryStatusText);
        model.addAttribute("discoveryIconName", discoveryIconName);
    }

    private void initializeAuthenticationAttributes(Model model) {
        String authStatusText = "The Authentication service is not running";
        String authIconName = "warning";
        boolean authUp = authorizationServiceUp();

        if (authUp) {
            authStatusText = "The Authentication service is running";
            authIconName = SUCCESS_ICON_NAME;
        }

        model.addAttribute("authStatusText", authStatusText);
        model.addAttribute("authIconName", authIconName);
    }

    private void initializeCatalogAttributes(Model model) {
        boolean isAnyCatalogAvailable = (apiCatalogServiceId != null && !apiCatalogServiceId.isEmpty());
        model.addAttribute("isAnyCatalogAvailable", isAnyCatalogAvailable);
        if (!isAnyCatalogAvailable) {
            return;
        }

        String catalogLink = null;
        String catalogStatusText = "The API Catalog is not running";
        String catalogIconName = "warning";
        boolean linkEnabled = false;
        boolean authServiceEnabled = authorizationServiceUp();

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(apiCatalogServiceId);
        if (serviceInstances != null && authServiceEnabled) {
            long catalogCount = serviceInstances.size();
            if (catalogCount == 1) {
                linkEnabled = true;
                catalogIconName = SUCCESS_ICON_NAME;
                catalogStatusText = "The API Catalog is running";
                catalogLink = getCatalogLink(serviceInstances.get(0));
            }
        }

        model.addAttribute("catalogLink", catalogLink);
        model.addAttribute("catalogIconName", catalogIconName);
        model.addAttribute("linkEnabled", linkEnabled);
        model.addAttribute("catalogStatusText", catalogStatusText);
    }

    private void initializeMetricsAttributes(Model model) {
        boolean metricsEnabled = true;
        model.addAttribute("metricsEnabled", metricsEnabled);

        boolean metricsLinkEnabled = false;
        String metricsLink = null;
        String metricsStatusText = "The Metrics service is not running";

        String metricsIconName = "warning";

        List<ServiceInstance> metricsServiceInstances = discoveryClient.getInstances("metrics");
        boolean metricsUp = !metricsServiceInstances.isEmpty();

        if (metricsUp && metricsEnabled) {
            metricsLinkEnabled = true;
            metricsIconName = SUCCESS_ICON_NAME;
            metricsLink = "http://localhost:10019/metrics/hystrix";
            metricsStatusText = "The Metrics service is running";
        }

        model.addAttribute("metricsLinkEnabled", metricsLinkEnabled);
        model.addAttribute("metricsStatusText", metricsStatusText);
        model.addAttribute("metricsIconName", metricsIconName);
        model.addAttribute("metricsLink", metricsLink);
    }

    private String getCatalogLink(ServiceInstance catalogInstance) {
        String gatewayUrl = catalogInstance.getMetadata().get(String.format("%s.ui-v1.%s", ROUTES, ROUTES_GATEWAY_URL));
        String serviceUrl = catalogInstance.getMetadata().get(String.format("%s.ui-v1.%s", ROUTES, ROUTES_SERVICE_URL));
        return serviceUrl + gatewayUrl;
    }

    private String getMetricsLink(ServiceInstance metricsInstance) {
        Map<String,String> data = metricsInstance.getMetadata();
        String serviceUrl = metricsInstance.getMetadata().get(String.format("%s.api-v1.%s", ROUTES, ROUTES_SERVICE_URL));
        return serviceUrl;
    }

    private boolean authorizationServiceUp() {
        if (providers.isZosfmUsed()) {
            return providers.isZosmfAvailable();
        }

        return true;
    }
}
