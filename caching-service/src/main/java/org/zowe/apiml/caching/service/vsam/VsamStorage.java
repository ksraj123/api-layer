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

    public static int RC_INVALID_VSAM_FILE = 1;

    private Map<String, Map<String, KeyValue>> storage = new HashMap<>();

    String filename = "//DD:VSMDATA";
    String options = "ab+,type=record";
    int lrecl = 80;
    int keyLen = 8;

    public VsamStorage(boolean isTestScope) {
        log.info("Using VSAM storage for the cached data");

        if (!isTestScope) {
            ZFile zfile = null;
            try {
                zfile = openZfile();
            } catch (ZFileException | RcException e) {
                log.error("Problem initializing VSAM storage, opening of {} in mode {} has failed", filename, options);
                log.error(e.toString());
                System.exit(RC_INVALID_VSAM_FILE);
            } finally {
                if (zfile != null) {
                    closeZfile(zfile);
                }
            }
        }
    }

    @Override
    public KeyValue create(String serviceId, KeyValue toCreate) {
        log.info("Writing record: {}|{}|{}", serviceId, toCreate.getKey(), toCreate.getValue());
        KeyValue result = null;
        ZFile zfile = null;
        try {
            zfile = openZfile();

            boolean found = zfile.locate(getCompositeKey(serviceId, toCreate.getKey()).getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE),
                ZFileConstants.LOCATE_KEY_EQ);

            if (!found) {
                byte[] record = padToLength(getCompositeKey(serviceId, toCreate) + toCreate.getValue(), lrecl)
                    .getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
                log.info("Writing Record: {}", new String(record, ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE));
                zfile.write(record);
                result = toCreate;
            } else {
                log.error("The record already exists and will not be created. Use update instead.");
            }


        } catch (ZFileException e) {
            log.error(e.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding: {}", ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
        } finally {
            closeZfile(zfile);
        }

        return result;
    }

    @Override
    public KeyValue read(String serviceId, String key) {
        log.info("Reading Record: {}|{}|{}", serviceId, key, "-");
        KeyValue result = null;
        ZFile zfile = null;
        try {
            zfile = openZfile();
            byte[] recBuf = new byte[lrecl];
            boolean found = zfile.locate(getCompositeKey(serviceId, key).getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE),
                ZFileConstants.LOCATE_KEY_EQ);
            log.info("Record found: {}", found);
            if (found) {
                zfile.read(recBuf);
                log.info("RecBuf: {}", recBuf);
                String value = new String(recBuf, ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
                log.info("ConvertedStringValue: {}", value);
                result = new KeyValue(key, value.substring(keyLen).trim());
            }
        } catch (ZFileException e) {
            log.error(e.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding: {}", ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
        } finally {
            closeZfile(zfile);
        }
        return result;
    }

    @Override
    public KeyValue update(String serviceId, KeyValue toUpdate) {
        log.info("Updating Record: {}|{}|{}", serviceId, toUpdate.getKey(), toUpdate.getValue());
        KeyValue result = null;
        ZFile zfile = null;
        try {
            zfile = openZfile();
            byte[] recBuf = new byte[lrecl];

            boolean found = zfile.locate(getCompositeKey(serviceId, toUpdate.getKey()).getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE),
                ZFileConstants.LOCATE_KEY_EQ);

            log.info("Record found: {}", found);

            if (found) {
                zfile.read(recBuf); //has to be read before update/delete
                byte[] record = padToLength(getCompositeKey(serviceId, toUpdate) + toUpdate.getValue(), lrecl)
                    .getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
                int nUpdated = zfile.update(record);
                log.info("record updated: {}", toUpdate);
                result = toUpdate;
            } else {
                log.error("No record updated because no record found with key");
            }
            // TODO exception?


        } catch (ZFileException e) {
            log.error(e.toString());
            //TODO what to return here if it doesn't work
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding: {}", ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
        } finally {
            closeZfile(zfile);
        }

        return result;
    }

    @Override
    public KeyValue delete(String serviceId, String toDelete) {

        log.info("Deleting Record: {}|{}|{}", serviceId, toDelete, "-");
        KeyValue result = null;
        ZFile zfile = null;

        try {
            zfile = openZfile();
            byte[] recBuf = new byte[lrecl];

            boolean found = zfile.locate(getCompositeKey(serviceId, toDelete).getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE),
                ZFileConstants.LOCATE_KEY_EQ);
            log.info("Record found: {}", found);

            if (found) {
                zfile.read(recBuf); //has to be read before update/delete
                zfile.delrec();
                log.info("record deleted: {}", toDelete);
                result = new KeyValue(toDelete, "DELETED");
            } else {
                log.error("No record deleted because no record found with key");
            }
            // TODO exception?


        } catch (ZFileException e) {
            log.error(e.toString());
            //TODO what to return here if it doesn't work
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding: {}", ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
        } finally {
            closeZfile(zfile);
        }

        return result;
    }

    @Override
    public Map<String, KeyValue> readForService(String serviceId) {
        throw new UnsupportedOperationException("Not Implemented");
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

    public ZFile openZfile() throws ZFileException, RcException {
        return ClassOrDefaultProxyUtils.createProxyByConstructor(ZFile.class, "com.ibm.jzos.ZFile",
            ZFileDummyImpl::new,
            new Class[] {String.class, String.class},
            new Object[] {filename, options},
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
        if (zfile != null) {
            try {
                zfile.close();
            } catch (ZFileException e) {
                log.error("Closing ZFile failed");
            }
        }
    }

    public String getCompositeKey(String serviceId, KeyValue keyValue) {
        return getCompositeKey(serviceId, keyValue.getKey());
    }

    public String getCompositeKey(String serviceId, String key) {

        int sidCapacity = 4;
        int keyCapacity = 4;

        StringBuilder b = new StringBuilder(keyLen);

        if (serviceId.length() > sidCapacity) {
            b.append(serviceId, 0, sidCapacity);
        } else {
            b.append(padToLength(serviceId, sidCapacity));
        }

        if (key.length() > keyCapacity) {
            b.append(key, 0, keyCapacity);
        } else {
            b.append(padToLength(key, keyCapacity));
        }

        return b.toString();
    }
}
