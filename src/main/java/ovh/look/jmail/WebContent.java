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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

public class WebContent {

    private static final double PADDING_VALUE = 2.0;
    private static final String buttonStyle = "-fx-font-weight: bold; -fx-font-size: 16px;";

    // Unicode characters for buttons
    private static final String backButtonUnicodeSymbol = "\u2190";
    private static final String forwardButtonUnicodeSymbol = "\u2192";
    private static final String reloadButtonUnicodeSymbol = "\u27F3";

    public static JFXPanel createContent(String webFileURL) {
        String urlString = Utils.toURLString(webFileURL);
        if (urlString == null) {
            return null;
        }
        BorderPane root = new BorderPane();
        WebView webView = new WebView();
        webView.setPrefSize(400, 300);
        WebEngine webEngine = webView.getEngine();
        webEngine.load(urlString);
        root.setCenter(webView);

        IntegerProperty historyLargestIndexProp = new SimpleIntegerProperty();
        ObservableList<WebHistory.Entry> historyEntries = webEngine.getHistory().getEntries();
        historyEntries.addListener((ListChangeListener.Change<? extends WebHistory.Entry> change) -> {
            historyLargestIndexProp.set(historyEntries.size() - 1);
        });

        BooleanExpression disableGoBackProp = Bindings.lessThanOrEqual(
                webEngine.getHistory().currentIndexProperty(), 0);

        BooleanExpression disableGoForwardProp = Bindings.greaterThanOrEqual(
                webEngine.getHistory().currentIndexProperty(),
                historyLargestIndexProp);

        final Button backButton = new Button(backButtonUnicodeSymbol);
        backButton.setStyle(buttonStyle);
        backButton.disableProperty().bind(disableGoBackProp);
        backButton.setOnAction(e -> webEngine.getHistory().go(-1));

        final Button forwardButton = new Button(forwardButtonUnicodeSymbol);
        forwardButton.setStyle(buttonStyle);
        forwardButton.disableProperty().bind(disableGoForwardProp);
        forwardButton.setOnAction(e -> webEngine.getHistory().go(+1));

        final Button reloadButton = new Button(reloadButtonUnicodeSymbol);
        reloadButton.setStyle(buttonStyle);
        reloadButton.setOnAction(e -> webEngine.reload());

        final HBox naviBar = new HBox();
        naviBar.getChildren().addAll(backButton, forwardButton, reloadButton);
        naviBar.setPadding(new Insets(PADDING_VALUE)); // Small padding in the navigation Bar
        root.setBottom(naviBar);

        Scene scene = new Scene(root, 400, 300);
        var jfxPanel = new JFXPanel();
        jfxPanel.setScene(scene);
        return jfxPanel;
    }
}
