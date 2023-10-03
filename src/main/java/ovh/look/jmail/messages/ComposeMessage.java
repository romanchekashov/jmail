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

package ovh.look.jmail.messages;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import ovh.look.jmail.MailStore;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ComposeMessage {

    private Stage stage;

    private TextField to;
    private TextField cc;
    private TextField subject;

    private HTMLEditor editor;

    // Compose new message
    public static void create() {
        new ComposeMessage(null);
    }

    // Reply to message
    public static void create(MailStore.MessageInfo messageInfo) {
        new ComposeMessage(messageInfo);
    }

    private void attachFile() {
        System.out.println("Attaching file...");
    }

    private void saveMessage() {
        System.err.println("Saving message in Drafts...");
    }

    private void sendMessage() {
        System.out.println("Sending message ... is not implemented");
    }

    private ComposeMessage(MailStore.MessageInfo messageInfo) {
        stage = new Stage();

        var root = new VBox();
        root.setPadding(new Insets(2));

        var compositionHeaderPane = new HBox();
        compositionHeaderPane.setPadding(new Insets(2));

        var sendButton = new Button("Send");
        sendButton.setOnAction(e -> {
            sendMessage();
            stage.hide();
        });
        compositionHeaderPane.getChildren().add(sendButton);

        var saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            saveMessage();
        });
        compositionHeaderPane.getChildren().add(saveButton);
        compositionHeaderPane.setAlignment(Pos.CENTER);

        var spacingPane = new Pane();
        HBox.setHgrow(spacingPane, Priority.ALWAYS);
        compositionHeaderPane.getChildren().add(spacingPane);

        var PAPERCLIP = "/images/paperclip.128x128.png";
        var paperClipImage  = new Image(getClass().getResourceAsStream(PAPERCLIP));
        var paperClipImageView = new ImageView(paperClipImage);
        paperClipImageView.setFitWidth(32);
        paperClipImageView.setFitHeight(32);
        var attachButton = new Button();
        attachButton.setGraphic(paperClipImageView);
        attachButton.setTooltip(new Tooltip("Add Attachment"));
        attachButton.setOnAction(e -> attachFile());
        compositionHeaderPane.getChildren().add(attachButton);

        root.getChildren().add(compositionHeaderPane);

        var meaasgeHeaderPane = new GridPane();
        meaasgeHeaderPane.setPadding(new Insets(2));

        var toLabel = new Label("To:");
        to = new TextField();

        var ccLabel = new Label("Cc:");
        cc = new TextField();

        var subjectLabel = new Label("Subject:");
        subject = new TextField();

        if (messageInfo != null) {
            to.setText(messageInfo.toField);
            cc.setText(messageInfo.ccField);
            String replySubject = messageInfo.subjectField;
            if (!messageInfo.subjectField.startsWith("Re:")) {
                replySubject = "Re: " + replySubject;
            }
            subject.setText(replySubject);
        }

        meaasgeHeaderPane.add(toLabel, 0, 0);
        meaasgeHeaderPane.add(to, 1, 0);
        meaasgeHeaderPane.add(ccLabel, 0, 1);
        meaasgeHeaderPane.add(cc, 1, 1);
        meaasgeHeaderPane.add(subjectLabel, 0, 2);
        meaasgeHeaderPane.add(subject, 1, 2);

        meaasgeHeaderPane.setHgap(5);
        meaasgeHeaderPane.setVgap(5);

        GridPane.setHalignment(toLabel, HPos.RIGHT);
        GridPane.setHalignment(ccLabel, HPos.RIGHT);
        GridPane.setHalignment(subjectLabel, HPos.RIGHT);

        GridPane.setHgrow(to, Priority.ALWAYS);
        GridPane.setHgrow(cc, Priority.ALWAYS);
        GridPane.setHgrow(subject, Priority.ALWAYS);

        root.getChildren().add(meaasgeHeaderPane);

        editor = new HTMLEditor();

        if (messageInfo != null) {
            String fileName = messageInfo.folderPath + File.separatorChar + messageInfo.bodyFileField;
            Path filePath = Path.of(fileName);
            String content = "";
            try {
                content = Files.readString(filePath);
            } catch (IOException e) {
            }

            if (!content.isEmpty()) {
                content = content
                        .replace("<html>", "")
                        .replace("<body>", "")
                        .replace("</html>", "")
                        .replace("</body>", "");
                content = "<p>On " + messageInfo.dateField + ", " + messageInfo.fromField + " wrote:"
                        + "<p>----- Included message -----"
                        + "<p>" + content;
                editor.setHtmlText(content);
            }
        }

        root.getChildren().add(editor);
        VBox.setVgrow(editor, Priority.ALWAYS);

        var scene = new Scene(root, 800, 800);

        // Setup binding so the title of the stage includes the subject
        StringExpression subjectExpr = Bindings.when(subject.textProperty().isEmpty())
                .then("(no subject)")
                .otherwise(subject.textProperty());

        var titleProperty = new SimpleStringProperty();
        titleProperty.bind(
                Bindings.concat("Compose: ", subjectExpr, " - JMail"));

        stage.titleProperty().bind(titleProperty);
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> {
            var save = new ButtonType("Save");
            var discard = new ButtonType("Discard");
            var cancel = ButtonType.CANCEL;
            var alert = new Alert(AlertType.CONFIRMATION,
                    "Save this message before closing window?",
                    save, discard, cancel);
            alert.setTitle("Save Message");
            alert.initOwner(stage);
            ButtonType result = alert.showAndWait().orElse(cancel);

            if (result == save) {
                saveMessage();
            } else if (result == discard) {
                System.err.println("Closing window without saving message");
            } else if (result == cancel) {
                e.consume();
            }
        });
    }
}
