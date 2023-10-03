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
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import ovh.look.jmail.MailStore;
import ovh.look.jmail.utils.Utils;

import java.awt.*;
import java.io.File;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FolderPane extends JFXPanel {

    record Item(
        String folderPath,    // Folder path (relative to mail store root)
        boolean isFolder,     // True if this item represents a folder
        String key,           // Column 1 sort key: folder or message seq num
        String name,          // Folder name or message Subject -- used as display value
        long size,            // Size of message including all attachments (0 for folder)
        Date date             // Date of message (Date last modified for folder)
    ) {}

    private static final NumberFormat nf = NumberFormat.getNumberInstance();
    private static final DateFormat df = new SimpleDateFormat("MM/d/yy hh:mm a");
    private static final int NAME_COLUMN_WIDTH = 200;
    private static final int SIZE_COLUMN_WIDTH = 100;
    private static final int DATE_COLUMN_WIDTH = 130;

    public FolderPane() {
        nf.setMaximumFractionDigits(2);
        nf.setRoundingMode(RoundingMode.CEILING);
        this.setMinimumSize(new Dimension(NAME_COLUMN_WIDTH, 0));
        this.setPreferredSize(new Dimension(NAME_COLUMN_WIDTH+SIZE_COLUMN_WIDTH, 0));
        Platform.runLater(() -> {
            var root = new BorderPane(createContent());
            this.setScene(new Scene(root));
        });
    }

    private final String jmailHome = MailStore.getMailStoreDir();

    private Parent createContent() {
        TreeItem<Item> root = createNode(".");
        root.setExpanded(true);

        final var treeTableView = new TreeTableView<Item>();
        treeTableView.setShowRoot(false);
        treeTableView.setRoot(root);
        treeTableView.setStyle("-fx-font-size: 10pt");

        // --- name column
        var nameColumn = new TreeTableColumn<Item, Item>("Folder / Subject");
        nameColumn.setPrefWidth(NAME_COLUMN_WIDTH);
        nameColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<Item>(p.getValue().getValue()));
        nameColumn.setCellFactory((final TreeTableColumn<Item, Item> p) ->
                new TreeTableCell<Item, Item>() {
            @Override protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    return;
                }

                setText(item.name);
            }
        });
        nameColumn.setComparator((Item i1, Item i2) -> i1.key.compareTo(i2.key));

        // --- size column
        TreeTableColumn<Item, Item> sizeColumn = new TreeTableColumn<Item, Item>("Size");
        sizeColumn.setPrefWidth(SIZE_COLUMN_WIDTH);
        sizeColumn.setCellValueFactory(p -> new ReadOnlyObjectWrapper<Item>(p.getValue().getValue()));
        sizeColumn.setCellFactory(p -> new TreeTableCell<Item, Item>() {
            @Override protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.isFolder) {
                    setText(null);
                    return;
                }

                var kBytes = item.size / 1024.0;
                var mBytes = kBytes / 1024.0;
                if (mBytes > 0.99) {
                    setText(nf.format(mBytes) + " MB");
                } else {
                    setText(nf.format(kBytes) + " KB");
                }
            }
        });
        sizeColumn.setComparator((Item i1, Item i2) -> Long.compare(i1.size, i2.size));

        // --- modified column
        var lastModifiedColumn = new TreeTableColumn<Item, Date>("Date");
        lastModifiedColumn.setPrefWidth(DATE_COLUMN_WIDTH);
        lastModifiedColumn.setCellValueFactory(p ->
                new ReadOnlyObjectWrapper<Date>(p.getValue().getValue().date));
        lastModifiedColumn.setCellFactory(p -> new TreeTableCell<Item, Date>() {
            @Override protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(df.format(item));
                }
            }
        });

        treeTableView.getColumns().setAll(nameColumn, sizeColumn, lastModifiedColumn);

        // Configure multiple row selection
        treeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        treeTableView.getSelectionModel().setCellSelectionEnabled(false);

        // Set sorting properties and initially sort on name column
        treeTableView.setSortMode(TreeSortMode.ALL_DESCENDANTS);
        nameColumn.setSortType(TreeTableColumn.SortType.ASCENDING);
        treeTableView.getSortOrder().clear();
        treeTableView.getSortOrder().add(nameColumn);
        treeTableView.sort();

        // Select inbox
        root.getChildren().stream()
                .filter(item -> "inbox".equalsIgnoreCase(item.getValue().name))
                .findFirst()
                .ifPresent(item -> {
                    treeTableView.getSelectionModel().select(item);
                });

        return treeTableView;
    }

    // Create a node for a given folder, this will process any sub-folders,
    // and read the messsage list
    private TreeItem<Item> createNode(final String folderPath) {

        // Recursively create sub-folders
        final var children = new ArrayList<TreeItem<Item>>();
        File folder = new File(jmailHome, folderPath);
        File[] files = folder.listFiles();
        if (files != null) {
            for (var file : files) {
                if (file.isDirectory()) {
                    TreeItem<Item> child;
                    if (".".equals(folderPath)) {
                        child = createNode(file.getName());
                    } else {
                        child = createNode(folderPath + File.separator + file.getName());
                    }
                    children.add(child);
                }
            }
        }

        // Read messages if msg-list.txt is present
        File msgListFile = new File(folder, "msg-list.txt");
        if (msgListFile.canRead()) {
            MailStore.MessageInfo[] messageInfos = MailStore.getMailStore()
                    .getMessages(folderPath);
            int seqNum = 0;
            for (var messageInfo : messageInfos) {
                TreeItem<Item> child = createNode(folderPath, seqNum, messageInfo);
                children.add(child);
                seqNum++;
            }
        }

        // Create item for this folder
        var path = folder.toPath();
        var folderName = path.getFileName().toString();
        String key;
        switch (folderName.toLowerCase(Locale.ROOT)) {
            case "inbox":
                key = "FOLDER00-" + folderName;
                break;
            case "drafts":
                key = "FOLDER01-" + folderName;
                break;
            case "sent":
                key = "FOLDER02-" + folderName;
                break;
            case "junk":
            case "spam":
                key = "FOLDER03-" + folderName;
                break;
            case "trash":
                key = "FOLDER04-" + folderName;
                break;
            default:
                key = "FOLDER99-" + folderName;
        }
        var lastModified = new Date(folder.lastModified());
        Item item = new Item(folderPath, true, key, folderName, 0, lastModified);
        final TreeItem<Item> node = new TreeItem<Item>(item) {
            {
                getChildren().setAll(children);
            }

            @Override
            public boolean isLeaf() {
                return false;
            }
        };
        return node;
    }

    private TreeItem<Item> createNode(String folderPath, int seqNum, MailStore.MessageInfo messageInfo) {
        var key = String.format("MSG-%04d", seqNum);
        var date = new Date(0);
        try {
            date = df.parse(messageInfo.dateField);
        } catch (ParseException ex) {
            System.err.println(ex);
        }
        var msgPath = Path.of(jmailHome, folderPath, messageInfo.bodyFileField);
        var size = msgPath.toFile().length();
        for (var attachment : messageInfo.attachments) {
            if (!Utils.isURL(attachment.attachmentName)) {
                var attPath = Path.of(jmailHome, folderPath, attachment.attachmentName);
                size += attPath.toFile().length();
            }
        }
        Item item = new Item(folderPath, false,
                key, messageInfo.subjectField, size, date);
        var node = new TreeItem<Item>(item) {
            @Override
            public boolean isLeaf() {
                return true;
            }
        };
        return node;
    }

}
