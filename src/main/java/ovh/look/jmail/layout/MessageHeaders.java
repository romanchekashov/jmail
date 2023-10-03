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

package ovh.look.jmail.layout;

import ovh.look.jmail.MailStore;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

public class MessageHeaders extends JScrollPane implements ListSelectionListener {

    MessagesArea msgArea;
    MessageHeadersTable headersTable;
    String folderName;
    MailStore.MessageInfo[] messages;
    int messageNumber = 0;

    MessageHeaders(String folder, MessagesArea msgArea) {
        this.folderName = folder;
        this.msgArea = msgArea;
        messages = MailStore.getMailStore().getMessages(folder);
        try {
            headersTable = createMessageHeaders(messages);
            headersTable.getSelectionModel().addListSelectionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setViewportView(headersTable);
    }

    public void valueChanged(ListSelectionEvent event) {
        messageNumber = headersTable.getSelectedRow();
        msgArea.updateMessagePane();
    }

    public MailStore.MessageInfo getCurrentMessage() {
        if (messages != null && messages.length > messageNumber) {
            return messages[messageNumber];
        } else {
            return null;
        }
    }


    public void previousMessage() {
        if (messageNumber > 0) {
            messageNumber -=1;
            headersTable.setRowSelectionInterval(messageNumber, messageNumber);
            headersTable.invalidate();
        }
    }

    public void nextMessage() {
        if (messageNumber < messages.length -1) {
            messageNumber +=1;
            headersTable.setRowSelectionInterval(messageNumber, messageNumber);
            headersTable.invalidate();
        }
    }

    public Dimension xgetPreferredSize() {
        return new Dimension(MessagesArea.PREFERRED_WIDTH, 200);
    }

    static class MessageHeadersTable extends JTable {

        public boolean isCellEditable(int row, int column) {
            return false;
        };

        MessageHeadersTable(Object[][] data, Object[] columns) {
            super(data, columns);
            setRowSelectionAllowed(true);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setRowSelectionInterval(0, 0);
            setShowVerticalLines(true);
        }
    }
    private MessageHeadersTable createMessageHeaders(MailStore.MessageInfo[] msgs) {

        Object[][] data = new Object[msgs.length][];
        for (int i=0; i<data.length; i++) {
            Object[] msg = new Object[3];
            msg[0] = msgs[i].subjectField;
            msg[1] = msgs[i].fromField;
            msg[2] = msgs[i].dateField;
            data[i] = msg;
        };

        String[] columns = { "Subject", "From", "Date" };

        MessageHeadersTable headers = new MessageHeadersTable(data, columns);
        return headers;
    }
}
