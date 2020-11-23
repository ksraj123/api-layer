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

public class ZFileDummyImpl implements ZFile {

    public ZFileDummyImpl() {
        throw new RuntimeException("Dummy Implementation should not be instantiated");
    }

    @Override
    public void close() throws ZFileException, RcException {

    }

    @Override
    public void delrec() throws ZFileException {

    }

    @Override
    public boolean locate(byte[] key, int options) throws ZFileException {
        return false;
    }

    @Override
    public boolean locate(byte[] key, int offset, int length, int options) throws ZFileException {
        return false;
    }

    @Override
    public boolean locate(long recordNumberOrRBA, int options) throws ZFileException {
        return false;
    }

    @Override
    public int read(byte[] buf) throws ZFileException {
        return 0;
    }

    @Override
    public int read(byte[] buf, int offset, int len) throws ZFileException {
        return 0;
    }

    @Override
    public int update(byte[] buf) throws ZFileException {
        return 0;
    }

    @Override
    public int update(byte[] buf, int offset, int length) throws ZFileException {
        return 0;
    }

    @Override
    public void write(byte[] buf) throws ZFileException {

    }

    @Override
    public void write(byte[] buf, int offset, int len) throws ZFileException {

    }

    @Override
    public String getActualFilename() {
        return null;
    }
}
