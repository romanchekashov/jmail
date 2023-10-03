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

public class MessagesArea extends JSplitPane {

    static int PREFERRED_WIDTH = 900;
    String folderName;
    MessageHeaders messageHeaders;
    MessagePane messagePane;
    JScrollPane sPane;

    public MessagesArea(String folder) {
        super(JSplitPane.VERTICAL_SPLIT, true);
        folderName = folder;

        messageHeaders = new MessageHeaders(folder, this);
        setTopComponent(messageHeaders);

        // create the pane displaying the messages for the current folder
        messagePane = new MessagePane(messageHeaders.getCurrentMessage());
        sPane = new JScrollPane(messagePane);
        setBottomComponent(sPane);
        setPreferredSize(new Dimension(PREFERRED_WIDTH, 0));
        SwingUtilities.invokeLater(() -> setDividerLocation(0.333));
    }

    public void nextMessage() {
            messageHeaders.nextMessage();
        }

    public void previousMessage() {
        messageHeaders.previousMessage();
    }

    public void updateMessagePane() {
        messagePane = new MessagePane(messageHeaders.getCurrentMessage());
        sPane.setViewportView(messagePane);
        invalidate();
    }

}
