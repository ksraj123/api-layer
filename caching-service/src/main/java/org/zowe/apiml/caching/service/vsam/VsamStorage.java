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
import org.zowe.apiml.caching.model.KeyValue;
import org.zowe.apiml.caching.service.Storage;
import org.zowe.apiml.util.ClassOrDefaultProxyUtils;
import org.zowe.apiml.zfile.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VsamStorage implements Storage {
    private Map<String, Map<String, KeyValue>> storage = new HashMap<>();

    String filename = "//DD:VSMDATA";
    String options = "ab+,type=record";
    int lrecl = 80;
    int keyLen = 8;
    ZFile zfile;



    public VsamStorage() {
        log.info("Using VSAM storage for the cached data");

        zfile = ClassOrDefaultProxyUtils.createProxy(ZFile.class, "com.ibm.jzos.ZFile",
            ZFileDummyImpl::new ,
            new ClassOrDefaultProxyUtils.ByMethodName<>(
            "com.ibm.jzos.ZFileException", ZFileException.class,
                "getFileName", "getMessage", "getErrnoMsg", "getErrno", "getErrno2", "getLastOp", "getAmrcBytes"),
            new ClassOrDefaultProxyUtils.ByMethodName<>(
                "com.ibm.jzos.RcException", RcException.class,
                "getMessage", "getRc"),
            new ClassOrDefaultProxyUtils.ByMethodName<>(
                "com.ibm.jzos.EnqueueException", EnqueueException.class,
                "getMessage", "getRc")
        );

        if (zfile == null) {
            log.error("ZFile was not instantiated");
            throw new RuntimeException("ZFile was not instantiated");
        } else {
            log.info("ZFile instantiated");
        }
    }

    @Override
    public KeyValue create(String serviceId, KeyValue toCreate) {
        storage.computeIfAbsent(serviceId, k -> new HashMap<>());
        Map<String, KeyValue> serviceStorage = storage.get(serviceId);
        serviceStorage.put(toCreate.getKey(), toCreate);
        return toCreate;
    }

    @Override
    public KeyValue read(String serviceId, String key) {
        Map<String, KeyValue> serviceSpecificStorage = storage.get(serviceId);
        if (serviceSpecificStorage == null) {
            return null;
        }

        return serviceSpecificStorage.get(key);
    }

    @Override
    public KeyValue update(String serviceId, KeyValue toUpdate) {
        String keyToUpdate = toUpdate.getKey();
        if (isKeyNotInCache(serviceId, keyToUpdate)) {
            return null;
        }

        Map<String, KeyValue> serviceStorage = storage.get(serviceId);
        serviceStorage.put(keyToUpdate, toUpdate);
        return toUpdate;
    }

    @Override
    public KeyValue delete(String serviceId, String toDelete) {
        if (isKeyNotInCache(serviceId, toDelete)) {
            return null;
        }

        Map<String, KeyValue> serviceSpecificStorage = storage.get(serviceId);
        return serviceSpecificStorage.remove(toDelete);
    }

    @Override
    public Map<String, KeyValue> readForService(String serviceId) {
        return storage.get(serviceId);
    }

    private boolean isKeyNotInCache(String serviceId, String keyToTest) {
        Map<String, KeyValue> serviceSpecificStorage = storage.get(serviceId);
        return serviceSpecificStorage == null || serviceSpecificStorage.get(keyToTest) == null;
    }
}
