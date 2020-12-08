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
import org.zowe.apiml.caching.model.KeyValue;
import org.zowe.apiml.zfile.ZFileConstants;
import org.zowe.apiml.zfile.ZFileException;

import java.util.List;

@Component
@Slf4j
public class VsamTestClass {

    private final VsamConfig config;
    private final VsamKey key;

    private final static String DATASET = "//'JANDA06.CACHE5'";
    private VsamStorage storage;

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

        VsamConfig config2 = new VsamConfig();
        config2.setFileName(DATASET);
        config2.setEncoding(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
        config2.setKeyLength(32);
        config2.setRecordLength(512);

        storage = new VsamStorage(config1);

        Runnable read = () -> {
            log.info("Executing READ VSAM thread");
            try (VsamFile file = new VsamFile(config1, VsamConfig.VsamOptions.READ)) {
                VsamRecord record = new VsamRecord(config1, "service1", new KeyValue("key1", ""));
                file.read(record);

                //Thread.sleep(10000);
            }
            log.info("handle released");
        };

        Runnable write = () -> {
            log.info("Executing WRITE VSAM thread");
            try (VsamFile file = new VsamFile(config2, VsamConfig.VsamOptions.WRITE)) {
                file.warmUpVsamFile();
                //Thread.sleep(15000);
            } catch (ZFileException | VsamRecordException e) {
                e.printStackTrace();
            }
            log.info("handle released");
        };

        Runnable fullShebang = () -> {
            log.info("Executing FULL SHEBANG VSAM thread");
            try (VsamFile file = new VsamFile(config2, VsamConfig.VsamOptions.WRITE)) {
                file.warmUpVsamFile();

                VsamRecord record1 = new VsamRecord(config1, "service1", new KeyValue("key1", "value1"));
                VsamRecord record1u = new VsamRecord(config1, "service1", new KeyValue("key1", "value2"));
                VsamRecord record2 = new VsamRecord(config1, "service1", new KeyValue("key2", "value1"));
                VsamRecord record3 = new VsamRecord(config1, "service1", new KeyValue("key3", "value1"));
                VsamRecord record4 = new VsamRecord(config1, "service1", new KeyValue("key4", "value1"));

                VsamRecord record5 = new VsamRecord(config1, "service2", new KeyValue("key1", "value1"));
                VsamRecord record6 = new VsamRecord(config1, "service2", new KeyValue("key2", "value1"));


                file.create(record1);
                file.create(record2);
                file.create(record3);
                file.create(record4);
                file.create(record5);
                file.create(record6);


                file.read(record1);


                file.update(record1u);

                file.delete(record1u);

                List<VsamRecord> service1 = file.readForService("service1");

                log.info("Read records: {}", service1);

                Thread.sleep(15000);
                log.info("handle released");

            } catch (ZFileException | VsamRecordException | InterruptedException e) {
                e.printStackTrace();
            }


        };

        Runnable storageDelete = () -> {
            log.info("Executing Storage Delete thread");
            storage.delete("service2", "key1");
        };


        new Thread(fullShebang).start();
        new Thread(storageDelete).start();
        new Thread(storageDelete).start();
        new Thread(storageDelete).start();
        new Thread(read).start();
        new Thread(read).start();
        new Thread(read).start();
        new Thread(read).start();



    }




}
