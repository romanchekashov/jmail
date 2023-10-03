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

import java.io.File;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Static utilities
 */
public class Utils {
    // No instance of this class
    private Utils() {}

    public static boolean isURL(String fileOrUrl) {
        fileOrUrl = fileOrUrl.toLowerCase(Locale.ROOT);
        return  fileOrUrl.startsWith("file:") ||
                fileOrUrl.startsWith("http:") ||
                fileOrUrl.startsWith("https:");
    }

    public static String toURLString(String fileOrUrl) {
        try {
            String urlString = fileOrUrl;
            if (!isURL(fileOrUrl)) {
                File file = new File(fileOrUrl);
                if (!file.canRead()) {
                    System.err.println("Unable to read: " + file);
                    return null;
                }
                urlString = file.toURI().toURL().toExternalForm();
            } else {
                var url = new URL(fileOrUrl);
                var in = url.openStream();
                if (in == null) {
                    System.err.println("Unable to read: " + url);
                    return null;
                }
                in.close();
            }
            return urlString;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Exception loading: " + fileOrUrl);
            return null;
        }
    }

    public static void runAndWaitOnFxThread(Runnable runnable) {
        var thr = new AtomicReference<Throwable>(null);
        var latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new RuntimeException("Timeout waiting for FX runnable to complete");
            }
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        if (thr.get() != null) {
            if (thr.get() instanceof RuntimeException rte) {
                throw rte;
            } else if (thr.get() instanceof Error err) {
                throw err;
            } else {
                throw new RuntimeException(thr.get());
            }
        }
    }

}
