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

import lombok.Getter;
import lombok.ToString;

import java.io.IOException;

@Getter
@ToString
public class ZFileException extends IOException {

    private String fileName;
    private String msg;
    private String errnoMsg;
    private int errno;
    private int errno2;
    private int lastOp;
    private byte[] amrc_code_bytes;
    private int abendCode;
    private int abendRc;
    private int feedbackRc;
    private int feedbackFtncd;
    private int feedbackFdbk;
    private int allocSvc99Error;
    private int allocSvc99Info;


    public ZFileException(String fileName, String msg, String errnoMsg, int errno, int errno2, int lastOp, byte[] amrc_code_bytes,
                          int abendCode, int abendRc, int feedbackRc, int feedbackFtncd, int feedbackFdbk /*, int allocSvc99Error, int allocSvc99Info*/) {
        this.fileName = fileName;
        this.msg = msg;
        this.errnoMsg = errnoMsg;
        this.errno = errno;
        this.errno2 = errno2;
        this.lastOp = lastOp;
        this.amrc_code_bytes = amrc_code_bytes;
        this.abendCode = abendCode;
        this.abendRc = abendRc;
        this.feedbackRc = feedbackRc;
        this.feedbackFtncd = feedbackFtncd;
        this.feedbackFdbk = feedbackFdbk;
        //this.allocSvc99Error = allocSvc99Error;
        //this.allocSvc99Info = allocSvc99Info;
    }
}
