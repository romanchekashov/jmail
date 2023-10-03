/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package ovh.look.jmail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MailStore {

    static MailStore mailStore;
    static MailStore getMailStore() {
        getMailStoreDir();
        if (mailStore == null) {
            mailStore = new MailStore();
        }
        return mailStore;
    }

    private static String rootDir = null;
    static String getMailStoreDir() {
        if (rootDir == null) {
            String dir = System.getProperty("mail_store");
            if (dir == null) {
                dir = System.getenv("MAIL_STORE");
            }
            if (dir == null) {
                dir = MailStore.class.getClassLoader().getResource("content/jmail-store").getPath();
//                dir = System.getProperty("user.home") + File.separatorChar + "jmail-store";
            }
            rootDir = dir;
        }
        return rootDir;
    }

    String[] getTopLevelFolders() {
        return getFolderList(getMailStoreDir());
    }

    String[] getFolderList(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles();
        String[] names = new String[files.length];

        for (int i=0; i<files.length; i++) {
            names[i] = files[i].getName();
        }
        return names;
    }

    boolean isFolder(String path) {
        File f = new File(path);
        return f.isDirectory();
    }

    String[] getMessageFiles(String folderPath) {
        String fullPath = getMailStoreDir() + File.separatorChar +
                folderPath + File.separatorChar + "msg-list.txt";
        File msgListFile = new File(fullPath);
        List<String> msgFiles = new ArrayList<String>();

        try (FileReader fr = new FileReader(msgListFile)) {
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                msgFiles.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return msgFiles.toArray(new String[0]);
    }

    MessageInfo[] getMessages(String folder) {
        String[] files = getMessageFiles(folder);
        MessageInfo[] infos = new MessageInfo[files.length];
        for (int i=0; i < files.length; i++) {
            try {
                infos[i] = getMessageInfo(folder, files[i]);
            } catch (Throwable t) { t.printStackTrace(); }
        }
        return infos;
    }

    static class AttachmentDesc {
        String attachmentType;
        String attachmentName;

        public AttachmentDesc(String aType, String aName) {
            attachmentType = aType;
            attachmentName = aName;
        }
    }

    static class MessageInfo {
        String folderPath;
        String fileName;
        String toField;
        String fromField;
        String ccField;
        String subjectField;
        String dateField;
        String bodyFileField;
        AttachmentDesc[] attachments;
    }

    MessageInfo getMessageInfo(String folder, String msgFile) {
        String folderPath = getMailStoreDir() + File.separatorChar + folder;
        String file = folderPath + File.separatorChar + msgFile;
        Properties props = new Properties();
        try (FileReader fr = new FileReader(file)) {
            props.load(fr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MessageInfo info = new MessageInfo();

        info.folderPath = folderPath;
        info.fileName = msgFile;
        info.toField = props.getProperty("to", "<empty>");
        info.ccField = props.getProperty("cc", "");
        info.fromField = props.getProperty("from", "");
        info.subjectField = props.getProperty("subject", "");
        info.dateField = props.getProperty("datetime", "");
        info.bodyFileField = props.getProperty("body", null);

        List<AttachmentDesc>  attDescs = new ArrayList<AttachmentDesc>();

        int aNo = 1;
        String val = null;
        do {
            String key = "attachment" + aNo;
            val = props.getProperty(key);
            if (val != null) {
                String typeKey = "type-attachment" + aNo;
                String typeVal = props.getProperty(typeKey);
                if (typeVal != null) {
                    attDescs.add(new AttachmentDesc(typeVal, val));
                }
            }
            aNo++;
        } while (val != null);
        info.attachments = attDescs.toArray(new AttachmentDesc[0]);

        return info;
    }

}
