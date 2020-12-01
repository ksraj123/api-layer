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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VsamTestClass {


    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("== Starting VSAM test class ==");
        log.info("== Starting VSAM test class ==");
        log.info("== Starting VSAM test class ==");
        log.info("== Starting VSAM test class ==");
        log.info("== Starting VSAM test class ==");
        log.info("== Starting VSAM test class ==");
        log.info("== Starting VSAM test class ==");
        log.info("== Starting VSAM test class ==");
    }


}
