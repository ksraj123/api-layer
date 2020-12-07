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
import org.zowe.apiml.caching.config.VsamConfig;
import org.zowe.apiml.zfile.ZFileConstants;
import org.zowe.apiml.zfile.ZFileException;

import java.io.UnsupportedEncodingException;

@Component
//@RequiredArgsConstructor
@Slf4j
public class VsamTestClass {

    private final VsamConfig config;
    private final VsamKey key;

    private final static String DATASET = "//'JANDA06.CACHE5'";

    public VsamTestClass(VsamConfig config) {
        this.config = config;
        this.key = new VsamKey(config);
    }



    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("== Starting VSAM test class ==");

        VsamConfig config1 = new VsamConfig();
        config1.setFileName(DATASET);
        config1.setEncoding(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
        config1.setKeyLength(32);
        config1.setRecordLength(512);
        config1.setOptions("rb,type=record");

        VsamConfig config2 = new VsamConfig();
        config2.setFileName(DATASET);
        config2.setEncoding(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
        config2.setKeyLength(32);
        config2.setRecordLength(512);
        config2.setOptions("ab+,type=record");


        Runnable read = () -> {
            log.info("Executing VSAM thread");
            try (VsamFile file = new VsamFile(config1)) {
                //file.warmUpVsamFile();
                readRecord(file, config1);
                Thread.sleep(10000);
            } catch (InterruptedException | ZFileException | VsamRecordException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            log.info("handle released");
        };

        Runnable write = () -> {
            log.info("Executing VSAM thread");
            try (VsamFile file = new VsamFile(config2)) {
                file.warmUpVsamFile();
                Thread.sleep(15000);
            } catch (ZFileException | VsamRecordException | InterruptedException e) {
                e.printStackTrace();
            }
            log.info("handle released");
        };

        new Thread(write).start();

        /*new Thread(write).start();

        new Thread(read).start();

        new Thread(write).start();*/

    }

    public void readRecord(VsamFile zfile, VsamConfig config) throws VsamRecordException, UnsupportedEncodingException, ZFileException {
        boolean found = zfile.locate(this.key.getKeyBytes("apimtst", "dodo"),
            ZFileConstants.LOCATE_KEY_EQ);
        log.info("RECORD_FOUND_MESSAGE", found);
        if (found) {
            byte[] recBuf = new byte[config.getRecordLength()];
            zfile.read(recBuf);
            log.info("RecBuf: {}", recBuf);
            log.info("ConvertedStringValue: {}", new String(recBuf, config.getEncoding()));
            VsamRecord record = new VsamRecord(config, "apimtst", recBuf);
            log.info("VsamRecord read: {}", record);
        }
    }


}
