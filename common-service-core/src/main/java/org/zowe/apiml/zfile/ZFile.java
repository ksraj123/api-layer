/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

package org.zowe.apiml.zfile;

public interface ZFile {

    void close();

    void delrec();

    void doDeqAndUnalloc();

    void flush();

    java.lang.String getActualFilename();

    int getBlksize();

    long getByteCount();

    int getDevice();

    int getDsorg();

    java.lang.String getFilename();

    java.io.InputStream getInputStream();

    int getLrecl();

    int getModeFlags();

    int getOpenMode();

    java.lang.String getOptions();

    java.io.OutputStream getOutputStream();

    byte[] getPos();

    java.lang.String getRecfm();

    int getRecfmBits();

    long getRecordCount();

    int getVsamKeyLength();

    long getVsamRBA();

    int getVsamType();

    boolean locate(byte[] key, int options);

    boolean locate(byte[] key, int offset, int length, int options);

    boolean locate(long recordNumberOrRBA, int options);

    int read(byte[] buf);

    int read(byte[] buf, int offset, int len);

    void reopen(java.lang.String options);

    void rewind();

    void seek(long offset, int origin);

    void setPos(byte[] position);

    long tell();

    int update(byte[] buf);

    int update(byte[] buf, int offset, int length);

    void write(byte[] buf);

    void write(byte[] buf, int offset, int len);

}
