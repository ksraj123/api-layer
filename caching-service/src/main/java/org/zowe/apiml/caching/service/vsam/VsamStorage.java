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

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VsamStorage implements Storage {
    private Map<String, Map<String, KeyValue>> storage = new HashMap<>();

    String filename = "//DD:VSMDATA";
    String options = "ab+,type=record";
    int lrecl = 80;
    int keyLen = 8;


    public VsamStorage() {
        log.info("Using VSAM storage for the cached data");

    }

    @Override
    public KeyValue create(String serviceId, KeyValue toCreate) {
        log.info("Writing ServiceId:KeyValue: {}: {}, {}", serviceId, toCreate.getKey(), toCreate.getValue());
        ZFile zfile = openZfile();
        try {

            byte[] record = padToLength("AAAAAAAARecord 1", lrecl)
                .getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);

            log.info("Writing Record: {}", record.toString());
            zfile.write(record);

            byte[] recBuf = new byte[lrecl];
            boolean found = zfile.locate(record, 0, keyLen, ZFileConstants.LOCATE_KEY_EQ);
            log.info("Record found: {}", found);

            zfile.read(recBuf);
            log.info("RecBuf: {}", recBuf);

        } catch (ZFileException | UnsupportedEncodingException e) {
            log.error(String.valueOf(e.getCause()));
            log.error(e.toString());

            e.printStackTrace();
        } finally {
            closeZfile(zfile);
        }

        return toCreate;
    }

    @Override
    public KeyValue read(String serviceId, String key) {
        log.info("Reading ServiceId:Key: {}: {}", serviceId, key);
        KeyValue result = null;
        ZFile zfile = openZfile();

        try {
            byte[] recBuf = new byte[lrecl];
            byte[] record = padToLength("AAAAAAAARecord 1", lrecl)
                .getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
            boolean found = zfile.locate(record, 0, keyLen, ZFileConstants.LOCATE_KEY_EQ);
            log.info("Record found: {}", found);

            zfile.read(recBuf);
            log.info("RecBuf: {}", recBuf);

            result = new KeyValue("KEY", recBuf.toString());

        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
        } finally {
            closeZfile(zfile);
        }

        return result;
    }

    @Override
    public KeyValue update(String serviceId, KeyValue toUpdate) {
        log.info("Updating ServiceId:Key: {}: {}", serviceId, toUpdate);
        ZFile zfile = openZfile();

        try {
            byte[] recBuf = new byte[lrecl];
            byte[] record = padToLength("AAAAAAAARecord 1 updated", lrecl)
                .getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);

            //TODO this form of locate seems nicer
//            byte[] keybuf = new byte[keyLen];
//            System.arraycopy(rec_2, 0, keybuf, 0, keyLen);
//            check("Locate rec_2",
//                zfile.locate(keybuf, ZFile.LOCATE_KEY_EQ));

            boolean found = zfile.locate(record, 0, keyLen, ZFileConstants.LOCATE_KEY_EQ);
            log.info("Record found: {}", found);

            if (found) {
                int nUpdated = zfile.update(record);
                log.info("record updated: {}", toUpdate);
            } else {
                log.error("No record updated because no record found with key");
                return null;
            }
            // TODO exception?


        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
        } finally {
            closeZfile(zfile);
        }

        return toUpdate;
    }

    @Override
    public KeyValue delete(String serviceId, String toDelete) {

        log.info("Deleting ServiceId:Key: {}: {}", serviceId, toDelete);
        ZFile zfile = openZfile();

        try {
            //byte[] keybuf = new byte[keyLen];
            String key = "AAAAAAAA";
            boolean found = zfile.locate(key.getBytes(), ZFileConstants.LOCATE_KEY_EQ);
            log.info("Record found: {}", found);

            if (found) {
                zfile.delrec();
                log.info("record deleted: {}", toDelete);
            } else {
                log.error("No record deleted because no record found with key");
                return null;
            }
            // TODO exception?


        } catch (Exception e) {
            log.error(e.toString());
            e.printStackTrace();
        } finally {
            closeZfile(zfile);
        }

        return new KeyValue(toDelete, "DELETED");
    }

    @Override
    public Map<String, KeyValue> readForService(String serviceId) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    private boolean isKeyNotInCache(String serviceId, String keyToTest) {
        Map<String, KeyValue> serviceSpecificStorage = storage.get(serviceId);
        return serviceSpecificStorage == null || serviceSpecificStorage.get(keyToTest) == null;
    }

    /**
     * Pad a string with spaces to a specified length
     */
    static String padToLength(String s, int len) {
        StringBuffer sb = new StringBuffer(len);
        sb.append(s);
        for (int i = s.length(); i < len; i++) sb.append(' ');
        return sb.toString();
    }

    public ZFile openZfile() {
        return ClassOrDefaultProxyUtils.createProxy(ZFile.class, "com.ibm.jzos.ZFile",
            ZFileDummyImpl::new,
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
    }

    public void closeZfile(ZFile zfile) {
        try {
            zfile.close();
        } catch (ZFileException e) {
            log.error("Closing ZFile failed");
        }
    }
}
