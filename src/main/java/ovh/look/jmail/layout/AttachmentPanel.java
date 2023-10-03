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

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import ovh.look.jmail.MailStore;
import ovh.look.jmail.core.FXContent;
import ovh.look.jmail.core.MediaControl;
import ovh.look.jmail.core.SwingContent;
import ovh.look.jmail.core.WebContent;
import ovh.look.jmail.utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class AttachmentPanel extends JComponent {

    public static JComponent create(String folderPath, MailStore.AttachmentDesc desc) {
          String contentType = desc.attachmentType;
          String fileName = desc.attachmentName;
          if (!Utils.isURL(fileName)) {
              fileName = folderPath + File.separatorChar + fileName;
          }
        JComponent c = null;
          switch (contentType.toLowerCase(Locale.ROOT)) {
              case "swing" : c = createSwingPanel(fileName); break;
              case "javafx" : c = createJavaFXPanel(fileName); break;
              case "image" : c = createImagePanel(fileName); break;
              case "media" : c = createMediaPanel(fileName); break;
              case "text" : c = createTextPanel(fileName); break;
              case "web" : c =  createWebPanel(fileName); break; // can also be a URL link
              default : c = null;
          }
          if (c == null) {
              return new JPanel();
          }
        c.setBorder(new TitledBorder(desc.attachmentName));

        return c;
    }

    static JComponent createTextPanel(String fileName) {
        String text = "Could not read file";
        Path filePath = Path.of(fileName);
        try {
            text = Files.readString(filePath);
        } catch (IOException e) {
        }
        JTextArea textArea = new JTextArea(text);
        textArea.setFont(new Font(Font.SERIF, Font.PLAIN, 14));
                return textArea;
    }

    static class ImagePanel extends JPanel {

        BufferedImage image;
        int w, h;
        ImagePanel(BufferedImage bi) {
            this.image = bi;
            w = image.getWidth();
            h = image.getHeight();
        }

        public Dimension getPreferredSize() {
            return new Dimension(400, 400);
        }
        public Dimension getMinimumSize() {
            return new Dimension(getPreferredSize().width/2,
                                getPreferredSize().height/2);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Dimension size = getSize();
            Insets insets = getInsets();
            int dw = w - (5 * (insets.left + insets.right));
            int dh = h - (10 * (insets.top + insets.bottom));
            if (dw > size.width || dw > size.height) {
                // need to scale down the image to fit.
                double xr = (double)size.width / w;
                double yr = (double)size.height / h;
                double scale = (xr < yr ) ? xr : yr;
                dw = (int)(dw * scale);
                dh = (int)(dh * scale);
            }
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                 RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(image, insets.left,  insets.top, dw, dh, null);
        }

    }

    static JPanel createImagePanel(String fileName) {
        try {
            BufferedImage bi = ImageIO.read(new File(fileName));
            if (bi == null) {
                return null;
            }
            return new ImagePanel(bi);
        } catch (IOException e) {
            e.printStackTrace();
           return null;
        }
    }

    static JPanel wrapJFXPanel(JFXPanel jfxPanel) {
        if (jfxPanel == null) {
            return new JPanel();
        }
        var panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(jfxPanel, BorderLayout.CENTER);
        return panel;
    }

    static void initFXPanel(JFXPanel jfxPanel, String fileName) {
        new FXContent(jfxPanel, fileName);

    }

    static JPanel createJavaFXPanel(String fileName) {
        JFXPanel jfxPanel = new JFXPanel();
        Platform.runLater(() -> initFXPanel(jfxPanel, fileName));
        jfxPanel.setMinimumSize(new Dimension(200, 200));
        return wrapJFXPanel(jfxPanel);
    }

    static JPanel createSwingPanel(String fileName) {
        JPanel jPanel = new JPanel();
        new SwingContent(jPanel, fileName);
        jPanel.setMinimumSize(new Dimension(200, 200));
        return jPanel;
    }

    private static JPanel createMediaPanel(String fileName) {
        AtomicReference<JFXPanel> jComponent = new AtomicReference<>(null);
        Utils.runAndWaitOnFxThread(() -> {
            JFXPanel jfxPanel = MediaControl.createContent(fileName);
            jComponent.set(jfxPanel);
        });
        return wrapJFXPanel(jComponent.get());
    }

    private static JPanel createWebPanel(String fileName) {
        AtomicReference<JFXPanel> jComponent = new AtomicReference<>(null);
        Utils.runAndWaitOnFxThread(() -> {
            JFXPanel jfxPanel = WebContent.createContent(fileName);
            jComponent.set(jfxPanel);
        });
        return wrapJFXPanel(jComponent.get());
    }

}
