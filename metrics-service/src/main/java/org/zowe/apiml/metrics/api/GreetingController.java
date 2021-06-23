/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */
package org.zowe.apiml.metrics.api;

import org.springframework.web.bind.annotation.*;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;


/**
 * Version 1 of the controller that returns greetings.
 */
@RestController
@RequestMapping("/")
public class GreetingController {
    private static final String TEMPLATE = "Hello, %s!";

    /**
     * Gets a greeting for anyone.
     */
    @GetMapping(value = "/greeting")
    @HystrixCommand()
    public String greeting(@RequestParam(value = "name", defaultValue = "world") String name,
                             @RequestParam(value = "delayMs", defaultValue = "0", required = false) Integer delayMs) {
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return String.format(TEMPLATE, name);
    }

    /**
     * Gets a custom greeting.
     */
    @GetMapping(value = {"/{yourName}/greeting"})
    public String customGreeting(@PathVariable(value = "yourName") String yourName) {
        return String.format(TEMPLATE, yourName);
    }
}

