/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.caching.service.vsam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.zowe.apiml.caching.config.VsamConfig;

@Component
@RequiredArgsConstructor
@Slf4j
public class VsamTestClass {

    private final VsamConfig config;

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("== Starting VSAM test class ==");

        Runnable exec = () -> {
            log.info("Executing VSAM thread");
            try (VsamFile file = new VsamFile(config)) {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        new Thread(exec).start();

        new Thread(exec).start();
    }


}
