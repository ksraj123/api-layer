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

public interface ZFileException {
    int getAbendCode();

    int getAbendRc();

    int getAllocSvc99Error();

    int getAllocSvc99Info();

    byte[] getAmrcBytes();

    int getErrno();

    int getErrno2();

    java.lang.String getErrnoMsg();

    int getErrorCode();

    int getFeedbackFdbk();

    int getFeedbackFtncd();

    int getFeedbackRc();

    java.lang.String getFileName();

    int getLastOp();

    java.lang.String getMessage();

    java.lang.String getSynadMsg();
}
