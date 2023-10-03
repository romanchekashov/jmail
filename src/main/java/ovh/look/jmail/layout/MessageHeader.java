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
import java.awt.*;

public class MessageHeader extends JPanel {

    String fromStr;
    String toStr;
    String subjectStr;
    String dateStr;

    public MessageHeader(MailStore.MessageInfo msgInfo) {

        GridBagLayout layout = new GridBagLayout();
        this.setLayout(layout);
        setBackground(Color.lightGray);

        JLabel fromText = new JLabel(msgInfo.fromField);
        JLabel fL = new JLabel("From: ");
        addPair(fL, fromText, layout);

        JLabel toText = new JLabel(msgInfo.toField);
        JLabel tL = new JLabel("To: ");
        addPair(tL, toText, layout);

        JLabel ccText = new JLabel(msgInfo.ccField);
        JLabel cL = new JLabel("Cc: ");
        addPair(cL, ccText, layout);

        JLabel subjText = new JLabel(msgInfo.subjectField);
        JLabel sL = new JLabel("Subject: ");
        addPair(sL, subjText, layout);

        JLabel dateText = new JLabel(msgInfo.dateField);
        JLabel dL = new JLabel("Date: ");
        addPair(dL, dateText, layout);
    }

    public Insets getInsets() {
        return new Insets(10,10,10,10);
    }
    private void addPair(JLabel desc, JLabel value, GridBagLayout layout) {

        desc.setForeground(Color.darkGray);
        value.setForeground(Color.black);
        desc.setLabelFor(value);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(desc, gbc);
        add(desc);

        gbc.weightx = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(value, gbc);
        add(value);
    }

    public void setContent(String fromStr, String toStr,
                           String subjectStr, String dateStr) {
        this.fromStr = fromStr;
        this.toStr = toStr;
        this.subjectStr = subjectStr;
        this.dateStr = dateStr;
    }
}
