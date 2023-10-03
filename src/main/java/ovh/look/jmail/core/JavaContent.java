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

package ovh.look.jmail.core;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class JavaContent {

    protected Object theUI;

    public JavaContent(String jarFileName) {
        try {
            JarFile jarFile = new JarFile(jarFileName);
            Manifest manifest = jarFile.getManifest();
            Attributes attributes = jarFile.getManifest().getMainAttributes();
            String mainClass = (String)attributes.get(Attributes.Name.MAIN_CLASS);
            mainClass = mainClass.replace('/', '.');
            Enumeration<JarEntry> e = jarFile.entries();

            File jFile = new File(jarFileName);
            URL fileURL = jFile.toURI().toURL();
            String jarURL = "jar:" + fileURL + "!/";
            URL url = new URL(jarURL);

            URL[] urls = { url };
            URLClassLoader urlCl = new URLClassLoader(urls);

            Class c = urlCl.loadClass(mainClass);
            Method contentMethod = c.getMethod("createContent");
            Object o = c.getDeclaredConstructor().newInstance();
            theUI = contentMethod.invoke(o);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }
}
