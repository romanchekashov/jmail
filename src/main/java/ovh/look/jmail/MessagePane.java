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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;

public class MessagePane extends JPanel {

    MessageHeader messageHeader;
    MessageBody messageBody;

    public MessagePane(MailStore.MessageInfo msg) {
        messageHeader = new MessageHeader(msg);
        messageBody = new MessageBody(msg);

        GridBagLayout mpLayout = new GridBagLayout();
        this.setLayout(mpLayout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.weightx = 1;
        mpLayout.setConstraints(messageHeader, gbc);

        gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        mpLayout.setConstraints(messageBody, gbc);

        add(messageHeader);
        add(messageBody);

        JComponent c;
        for (MailStore.AttachmentDesc d : msg.attachments) {
            c = AttachmentPanel.create(msg.folderPath, d);
            mpLayout.setConstraints(c, gbc);
            add(c);
        }

        c = new AttachmentButtonPanel(msg.folderPath, msg.attachments);

        mpLayout.setConstraints(c, gbc);
        add(c);
    }

    static class AttachmentButtonPanel extends JPanel implements ActionListener {
        String folderPath;
        MailStore.AttachmentDesc[] attachments;

        AttachmentButtonPanel(String folderPath,
                              MailStore.AttachmentDesc[] attachments) {
            this.folderPath = folderPath;
            this.attachments = attachments;
            int len = attachments.length;
            setLayout(new GridLayout(len, 1));
            for (MailStore.AttachmentDesc d : attachments) {
                addButton(d.attachmentName);
            }
        }

        void addButton(String s) {
            JButton b = JMail.createButton(
                    "images/paperclip.128x128.png", 16, s, null);
            b.setBorderPainted(false);
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setIconTextGap(10);
            b.addActionListener(this);
            add(b);
        }

        public void actionPerformed(ActionEvent e) {
            String s = e.getActionCommand();
            boolean isURL = Utils.isURL(s);
            if (!isURL) {
                s = folderPath + File.separatorChar + s;
            }

            if (s.endsWith(".jar")) {
                String javaHome = System.getProperty("java.home", "");
                var javaCmd = Path.of(javaHome, "bin", "java").toString();
                ProcessBuilder pb = new ProcessBuilder(javaCmd, "-jar", s);
                try {
                    pb.start();
                } catch (IOException ioe) {
                }
                return;
            }
            if (!Desktop.isDesktopSupported()) {
                return;
            }
            Desktop d = Desktop.getDesktop();
            if (isURL) {
                try {
                    d.browse(new URI(s));
                } catch (Exception urle) {
                    urle.printStackTrace();
                }
            } else {
                File f = new File(s);
                try {
                    d.open(f);
                } catch (IOException ioe) {
                }
            }
        }
    }

    MessageHeader getMessageHeader() {
        return messageHeader;
    }

    MessageBody getMessageBody() {
        return messageBody;
    }
}
