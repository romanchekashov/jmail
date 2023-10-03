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

import javafx.application.Platform;
import ovh.look.jmail.utils.AssetUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.AboutHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.lang.reflect.InvocationTargetException;


public class JMail {

    JFrame mainWindow;
    JToolBar toolBar;
    FolderPane folderPane;
    MessagesArea messagesArea;
    SystemTray systemTray;

    public static void main(String[] args) {
        handleSplashScreen();

        final JMail jm = new JMail();
        MailStore.getMailStore();
        try {
            SwingUtilities.invokeAndWait(() -> jm.createUI());
        } catch (InterruptedException | InvocationTargetException e) {
        }
    }

    // Get the preferred height for the message pane
    private int getPreferredHeight() {
        final int TOOLBAR_HEIGHT = 64;
        final int WINDOW_DECORATION_HEIGHT = 30;

        var gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();
        var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        var screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        var screenH = screenSize.height - screenInsets.top - screenInsets.bottom;
        return screenH - TOOLBAR_HEIGHT - WINDOW_DECORATION_HEIGHT;
    }

    public void createUI() {
        try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException |
                ClassNotFoundException |
                InstantiationException |
                IllegalAccessException e) {
        }
        // create the main window.
        mainWindow = new JFrame("JMail");
        try {
            Image iconImage = AssetUtils.getBufferedImage("images/mail.angle.128x128.png");
            mainWindow.setIconImage(iconImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagLayout mainLayout = new GridBagLayout();
        mainWindow.setLayout(mainLayout);

        // Add toolbar child at the top of the main window.
        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.fill = GridBagConstraints.HORIZONTAL;
        gbc1.gridwidth = GridBagConstraints.REMAINDER;
        toolBar = createToolBar();
        mainLayout.setConstraints(toolBar, gbc1);
        mainWindow.add(toolBar);

        folderPane = new FolderPane();
        messagesArea = new MessagesArea("inbox");

        Dimension preferredSize = messagesArea.getPreferredSize();
        preferredSize.height = getPreferredHeight();
        messagesArea.setPreferredSize(preferredSize);

        // Add to top level a split pane with folders on the left
        // and the view of the current folder on the right
        JSplitPane foldersAndMessages =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
                        folderPane, messagesArea);
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridwidth = GridBagConstraints.REMAINDER;
        gbc2.gridheight = GridBagConstraints.REMAINDER;
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.weightx = 1;
        gbc2.weighty = 1;
        mainLayout.setConstraints(foldersAndMessages, gbc2);
        mainWindow.add(foldersAndMessages);
        mainWindow.pack();
        mainWindow.setLocationRelativeTo(null);
        mainWindow.show();
        addSystemTrayIcon();
        addAboutHandler();
    }

    static ImageIcon loadIconImage(String image, int targetSize) {
        ImageIcon icon = null;
        if (image != null) {
            try {
                BufferedImage bi = AssetUtils.getBufferedImage(image);
                BufferedImage img = new BufferedImage(targetSize, targetSize, bi.getType());
                Graphics2D g2d = img.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2d.drawImage(bi, 0, 0, targetSize, targetSize, null);
                g2d.dispose();
                icon = new ImageIcon(img);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return icon;
    }
    static JButton createButton(String image, int targetSize, String text, String toolTip) {
        ImageIcon icon = loadIconImage(image, targetSize);
        JButton b = new JButton(text, icon);
        b.setToolTipText(toolTip);
        return b;
    }
    static JLabel createLabel(String image, int targetSize, String text) {
        ImageIcon icon = loadIconImage(image, targetSize);
        JLabel l = new JLabel(text, icon, SwingConstants.TRAILING);
        return l;
    }


    private JToolBar createToolBar() {
        JToolBar tBar = new JToolBar("jmail.JMail tools");
        tBar.setMargin(new Insets(5, 5, 5, 5));
        tBar.setFloatable(false);

        int size = 48;

        JButton getMsgs = createButton("images/refresh.128x128.png", size, null, "Check for new messages");

        JButton nextMsg = createButton("images/right-arrow.128x128.png", size, null, "Next message");
        nextMsg.addActionListener(e -> SwingUtilities.invokeLater(() -> messagesArea.nextMessage()));

        JButton previousMsg = createButton("images/left-arrow.128x128.png", size, null, "Previous message");
        previousMsg.addActionListener(e -> SwingUtilities.invokeLater(() -> messagesArea.previousMessage()));

        JButton writeMsg = createButton("images/pencil.2.128x128.png", size, null, "Write New Message");
        writeMsg.addActionListener(e -> Platform.runLater(() -> ComposeMessage.create()));

        JButton replyMsg = createButton("images/reply2.128x128.png", size, null, "Reply to current message");
        replyMsg.addActionListener(e -> {
            MailStore.MessageInfo messageInfo = messagesArea.messageHeaders.getCurrentMessage();
            Platform.runLater(() -> ComposeMessage.create(messageInfo));
        });

        JButton deleteMsg = createButton("images/trash.128x128.png", size, null,
                "Move current message to Trash Folder");

        JButton print = createButton("images/printer.128x128.png", size, null, "Print Message");
        print.addActionListener(e -> SwingUtilities.invokeLater(() -> printMessage()));

        tBar.add(getMsgs);
        tBar.addSeparator();
        tBar.add(nextMsg);
        tBar.addSeparator();
        tBar.add(previousMsg);
        tBar.addSeparator();
        tBar.add(writeMsg);
        tBar.addSeparator();
        tBar.add(replyMsg);
        tBar.addSeparator();
        tBar.add(deleteMsg);
        tBar.addSeparator();
        tBar.add(print);

        return tBar;
    }

    private void printMessage() {
        PrinterJob pj = PrinterJob.getPrinterJob();
        pj.printDialog();
    }

    private void addSystemTrayIcon() {
        try {
            systemTray = SystemTray.getSystemTray();
            String os = System.getProperty("os.name").toLowerCase();
            String icon = os.startsWith("mac") ? "mail.128x128.png" : "mail.16x16.png";
            Image trayImage = AssetUtils.getBufferedImage("images/" + icon);
            PopupMenu menu = new PopupMenu();
            MenuItem item = new MenuItem("Compose New Message");
            menu.add(item);
            item.addActionListener(e -> Platform.runLater(() -> ComposeMessage.create()));
            TrayIcon trayIcon = new TrayIcon(trayImage, "JMail_App", menu);
            trayIcon.displayMessage("caption", "Hello", TrayIcon.MessageType.INFO);
            systemTray.add(trayIcon);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class JMailAboutHandler implements AboutHandler {

        String infoText =
                """
                        <html><body>
                                    
                        <center>
                        JMail version 1.0
                        <br>
                        (c) 2022 Oracle Corporation, all rights reserved.
                        <br>
                        Authors : Phil Race, Kevin Rushforth
                        <br>
                        The good graphics images : Jeff Dinkins
                        <br>
                        </center>
                                    
                        </body><html>
                        """;


        public void handleAbout(AboutEvent ae) {
            SwingUtilities.invokeLater(() -> {
                final JDialog dlg = new JDialog((Frame) null, "About JMail");
                dlg.setBackground(Color.white);
                dlg.setLayout(new BorderLayout());
                JEditorPane info = new JEditorPane("text/html", infoText);
                info.setEditable(false);
                dlg.add(BorderLayout.CENTER, info);
                JLabel l = createLabel("images/mail.128x128.png", 32, null);
                JPanel lp = new JPanel();
                lp.setBackground(Color.white);
                lp.add(l);
                dlg.add(BorderLayout.WEST, lp);
                JPanel p = new JPanel();
                p.setBackground(Color.white);
                JButton dismiss = new JButton("OK");
                dismiss.addActionListener((ActionEvent event) -> {
                    dlg.setVisible(false);
                    dlg.dispose();
                });
                p.add(dismiss);
                dlg.add(BorderLayout.SOUTH, p);
                dlg.pack();
                dlg.setLocationRelativeTo(null);
                dlg.setVisible(true);
            });
        }
    }

    private void addAboutHandler() {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        Desktop dt = Desktop.getDesktop();
        if (!dt.isSupported(Desktop.Action.APP_ABOUT)) {
            return;
        }
        JMailAboutHandler dth = new JMailAboutHandler();
        dt.setAboutHandler(dth);
    }

    static class SplashWindow extends JWindow implements ActionListener {

        SplashScreen ss;
        BufferedImage si;
        volatile boolean firstPaint = true;
        Timer t;

        SplashWindow(SplashScreen ss) {
            super((Window) null);
            this.ss = ss;
            try {
                si = ImageIO.read(ss.getImageURL());
            } catch (Exception e) {
                ss.close();
            }
            setBounds(ss.getBounds());
            setAlwaysOnTop(true);
            setVisible(true);
        }

        public void paint(Graphics g) {
            g.drawImage(si, 0, 0, null);
            if (firstPaint && isShowing() ) {
                try {
                    ss.close();
                } catch (IllegalStateException ise) {
                }
                firstPaint = false;
                t = new Timer(50, this);
                t.start();
            }
        }

        public void actionPerformed(ActionEvent e) {
            float opacity = getOpacity() - 0.02f;
            if (opacity <= 0f) {
                this.setVisible(false);
                t.stop();
            } else {
                setOpacity(opacity);
                repaint();
            }
        }

    }

    static void handleSplashScreen() {
        SplashScreen ss = SplashScreen.getSplashScreen();
        if (ss == null) {
            return;
        }
        Graphics2D g2d = ss.createGraphics();
        GraphicsConfiguration gConf = g2d.getDeviceConfiguration();
        GraphicsDevice gDev = gConf.getDevice();
        boolean transSupported = gDev.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
        if (!transSupported) {
            ss.close();
            return;
        }
        try {
            EventQueue.invokeAndWait(() -> new SplashWindow(ss));
        } catch (Exception e) {
        }
    }
}
