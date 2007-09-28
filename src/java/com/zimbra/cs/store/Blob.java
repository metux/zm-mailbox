/*
 * ***** BEGIN LICENSE BLOCK *****
 * 
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2005 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * 
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on 2005. 6. 7.
 */
package com.zimbra.cs.store;

import java.io.File;

/**
 * @author jhahm
 * 
 * Represents a blob in blob store incoming directory.  An incoming blob
 * does not belong to any mailbox.  When a message is delivered to a mailbox,
 * message is saved in the incoming directory and a link to it is created
 * in the mailbox's directory.  The linked blob in mailbox directory
 * is represented by a MailboxBlob object.
 */
public class Blob {

    private File mFile;
    private String mPath;
    private short mVolumeId;

    public Blob(File file, short volumeId) {
        mFile = file;
        mPath = file.getAbsolutePath();
        mVolumeId = volumeId;
    }

    public File getFile() {
        return mFile;
    }

    public String getPath() {
    	return mPath;
    }

    public short getVolumeId() {
    	return mVolumeId;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("path=").append(mPath);
        sb.append(", vol=").append(mVolumeId);
        return sb.toString();
    }
}
