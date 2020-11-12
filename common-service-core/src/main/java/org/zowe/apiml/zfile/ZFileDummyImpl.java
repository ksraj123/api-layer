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

import java.io.InputStream;
import java.io.OutputStream;

public class ZFileDummyImpl implements ZFile {

    public ZFileDummyImpl() {
        throw new RuntimeException("Dummy Implementation should not be instantiated");
    }

    @Override
    public void close() {

    }

    @Override
    public void delrec() {

    }

    @Override
    public void doDeqAndUnalloc() {

    }

    @Override
    public void flush() {

    }

    @Override
    public String getActualFilename() {
        return null;
    }

    @Override
    public int getBlksize() {
        return 0;
    }

    @Override
    public long getByteCount() {
        return 0;
    }

    @Override
    public int getDevice() {
        return 0;
    }

    @Override
    public int getDsorg() {
        return 0;
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public int getLrecl() {
        return 0;
    }

    @Override
    public int getModeFlags() {
        return 0;
    }

    @Override
    public int getOpenMode() {
        return 0;
    }

    @Override
    public String getOptions() {
        return null;
    }

    @Override
    public OutputStream getOutputStream() {
        return null;
    }

    @Override
    public byte[] getPos() {
        return new byte[0];
    }

    @Override
    public String getRecfm() {
        return null;
    }

    @Override
    public int getRecfmBits() {
        return 0;
    }

    @Override
    public long getRecordCount() {
        return 0;
    }

    @Override
    public int getVsamKeyLength() {
        return 0;
    }

    @Override
    public long getVsamRBA() {
        return 0;
    }

    @Override
    public int getVsamType() {
        return 0;
    }

    @Override
    public boolean locate(byte[] key, int options) {
        return false;
    }

    @Override
    public boolean locate(byte[] key, int offset, int length, int options) {
        return false;
    }

    @Override
    public boolean locate(long recordNumberOrRBA, int options) {
        return false;
    }

    @Override
    public int read(byte[] buf) {
        return 0;
    }

    @Override
    public int read(byte[] buf, int offset, int len) {
        return 0;
    }

    @Override
    public void reopen(String options) {

    }

    @Override
    public void rewind() {

    }

    @Override
    public void seek(long offset, int origin) {

    }

    @Override
    public void setPos(byte[] position) {

    }

    @Override
    public long tell() {
        return 0;
    }

    @Override
    public int update(byte[] buf) {
        return 0;
    }

    @Override
    public int update(byte[] buf, int offset, int length) {
        return 0;
    }

    @Override
    public void write(byte[] buf) {

    }

    @Override
    public void write(byte[] buf, int offset, int len) {

    }
}
