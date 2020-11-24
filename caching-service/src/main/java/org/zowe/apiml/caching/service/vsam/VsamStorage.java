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
import org.zowe.apiml.caching.config.VsamConfig;
import org.zowe.apiml.caching.model.KeyValue;
import org.zowe.apiml.caching.service.Storage;
import org.zowe.apiml.util.ClassOrDefaultProxyUtils;
import org.zowe.apiml.util.ObjectUtil;
import org.zowe.apiml.zfile.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class VsamStorage implements Storage {

    public static int RC_INVALID_VSAM_FILE = 1;

    String options = "ab+,type=record";

    VsamConfig vsamConfig;
    VsamKey key;
    int keyLen;
    int lrecl;


    public VsamStorage(VsamConfig config, boolean isTestScope) {

        log.info("Using VSAM storage for the cached data");
        ObjectUtil.requireNotNull(config.getVsamFileName(), "Vsam filename cannot be null");
        ObjectUtil.requireNotEmpty(config.getVsamFileName(), "Vsam filename cannot be empty");

        log.info("Using Vsam configuration: {}", vsamConfig);

        this.vsamConfig = config;
        this.key = new VsamKey(config.getVsamKeyLength());
        this.keyLen = vsamConfig.getVsamKeyLength();
        this.lrecl = vsamConfig.getVsamRecordLength();

        if (!isTestScope) {
            warmUpVsamFile(config);
        }
    }

    public void warmUpVsamFile(VsamConfig config) {
        ZFile zfile = null;
        try {
            log.info("Warming up the vsam file by writing and deleting a record");
            byte[] recBuf = new byte[lrecl];
            zfile = openZfile();
            log.info("VSAM file being used: {}", zfile.getActualFilename());

            byte[] record = VsamUtils.padToLength(key.getKey("dele", "teme") + "warmup record, delete it", lrecl)
                .getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
            log.info("Writing Record: {}", new String(record, ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE));
            zfile.write(record);
            boolean found = zfile.locate(key.getKeyBytes("dele", "teme"),
                ZFileConstants.LOCATE_KEY_EQ);
            log.info("Test record for deletion found: {}", found);
            if (found) {
                zfile.read(recBuf); //has to be read before update/delete
                zfile.delrec();
                log.info("Test record deleted.");
            }
        } catch (ZFileException | RcException e) {
            log.error("Problem initializing VSAM storage, opening of {} in mode {} has failed", config, options);
            log.error(e.toString());
            System.exit(RC_INVALID_VSAM_FILE);
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding: {}", ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
        } finally {
            if (zfile != null) {
                closeZfile(zfile);
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

            boolean found = zfile.locate(key.getKeyBytes(serviceId, toCreate.getKey()),
                ZFileConstants.LOCATE_KEY_EQ);

            if (!found) {
                byte[] record = VsamUtils.padToLength(key.getKey(serviceId, toCreate) + toCreate.getValue(), lrecl)
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
            boolean found = zfile.locate(this.key.getKeyBytes(serviceId, key),
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

            boolean found = zfile.locate(key.getKeyBytes(serviceId, toUpdate.getKey()),
                ZFileConstants.LOCATE_KEY_EQ);

            log.info("Record found: {}", found);

            if (found) {
                zfile.read(recBuf); //has to be read before update/delete
                log.info("Read found record: {}", new String(recBuf, ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE));
                byte[] record = VsamUtils.padToLength(key.getKey(serviceId, toUpdate) + toUpdate.getValue(), lrecl)
                    .getBytes(ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
                log.info("Construct updated record: {}", new String(record, ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE));
                int nUpdated = zfile.update(record);
                log.info("record updated: {}", toUpdate);
                result = toUpdate;
            } else {
                log.error("No record updated because no record found with key");
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
    public KeyValue delete(String serviceId, String toDelete) {

        log.info("Deleting Record: {}|{}|{}", serviceId, toDelete, "-");
        KeyValue result = null;
        ZFile zfile = null;

        try {
            zfile = openZfile();
            byte[] recBuf = new byte[lrecl];

            boolean found = zfile.locate(key.getKeyBytes(serviceId, toDelete),
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
    public Map<String, KeyValue> readForService(String serviceId) {

        log.info("Reading All Records: {}|{}|{}", serviceId, "-", "-");
        Map<String, KeyValue> result = new HashMap<>();
        ZFile zfile = null;
        try {
            zfile = openZfile();
            byte[] recBuf = new byte[lrecl];

            boolean found;
            log.info("Attempt to find key in KEY_GE mode: {}", key.getKeySidOnly(serviceId));
            found = zfile.locate(key.getKeyBytesSidOnly(serviceId),
                ZFileConstants.LOCATE_KEY_GE);

            log.info("Record found: {}", found);

            int overflowProtection = 1000;
            while (found) {
                int nread = zfile.read(recBuf);
                log.info("RecBuf: {}", recBuf);
                log.info("nread: {}", nread);
                String value = new String(recBuf, ZFileConstants.DEFAULT_EBCDIC_CODE_PAGE);
                log.info("ConvertedStringValue: {}", value);

                if (nread < 0) {
                    log.info("nread is < 0, stopping the retrieval");
                    found = false;
                    continue;
                }

                //TODO values should be stored in JSON
                KeyValue record = new KeyValue(value.substring(0, keyLen), value.substring(keyLen).trim());

                result.put(record.getKey(), record);

                overflowProtection--;
                if (overflowProtection <= 0) {
                    log.error("Maximum number of records retrieved, stopping the retrieval");
                    break;
                }
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

    public ZFile openZfile() throws ZFileException, RcException {
        return ClassOrDefaultProxyUtils.createProxyByConstructor(ZFile.class, "com.ibm.jzos.ZFile",
            ZFileDummyImpl::new,
            new Class[] {String.class, String.class},
            new Object[] {vsamConfig.getVsamFileName(), options},
            new ClassOrDefaultProxyUtils.ByMethodName<>(
                "com.ibm.jzos.ZFileException", ZFileException.class,
                "getFileName", "getMessage", "getErrnoMsg", "getErrno", "getErrno2", "getLastOp", "getAmrcBytes",
                "getAbendCode", "getAbendRc", "getFeedbackRc", "getFeedbackFtncd", "getFeedbackFdbk"),
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

}
