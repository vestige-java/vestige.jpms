/*
 * This file is part of Vestige.
 *
 * Vestige is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vestige is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vestige.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.gaellalire.vestige.jpms;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gaellalire.vestige.jpms.ModuleDescriptor.Builder;
import fr.gaellalire.vestige.jpms.ModuleDescriptor.Modifier;

/**
 * @author Gael Lalire
 */
public final class NamedModuleUtils {

    private NamedModuleUtils() {
    }

    public static final String MODULE_INFO_ENTRY_NAME = "module-info.class";

    private static final Pattern DASH_VERSION = Pattern.compile("-(\\d+(\\.|$))");

    private static final Attributes.Name AUTOMATIC_MODULE_NAME = new Attributes.Name("Automatic-Module-Name");

    private static final Pattern NON_ALPHANUM = Pattern.compile("[^A-Za-z0-9]");

    private static final Pattern REPEATING_DOTS = Pattern.compile("(\\.)(\\1)+");

    // will be public when it will support automatic module
    private static ModuleDescriptor getDescriptor(final File file) throws IOException {
        String moduleName = null;
        JarFile jarFile = new JarFile(file);
        try {
            JarEntry jarEntry = jarFile.getJarEntry(MODULE_INFO_ENTRY_NAME);
            if (jarEntry != null) {

                InputStream inputStream = jarFile.getInputStream(jarEntry);
                try {
                    return getDescriptor(inputStream);
                } finally {
                    inputStream.close();
                }
            }
            if (moduleName == null) {
                // Read Automatic-Module-Name attribute if present
                Manifest man = jarFile.getManifest();
                moduleName = getAutomaticModuleName(man);

            }
        } finally {
            jarFile.close();
        }
        if (moduleName == null) {
            moduleName = getAutomaticModuleName(file.getName());
        }
        Builder builder = new Builder(moduleName, false, Collections.<Modifier> emptySet());
        return builder.build();
    }

    public static ModuleDescriptor getDescriptor(final InputStream moduleInfoInputStream) throws IOException {
        return new ModuleInfo().doRead(new DataInputStream(moduleInfoInputStream));
    }

    public static String getAutomaticModuleName(final Manifest man) {
        Attributes attrs = null;
        if (man != null) {
            attrs = man.getMainAttributes();
            if (attrs != null) {
                return attrs.getValue(AUTOMATIC_MODULE_NAME);
            }
        }
        return null;
    }

    public static String getAutomaticModuleName(final String fn) {
        String moduleName;

        // drop ".jar"
        moduleName = fn.substring(0, fn.length() - 4);

        // find first occurrence of -${NUMBER}. or -${NUMBER}$
        Matcher matcher = DASH_VERSION.matcher(moduleName);
        if (matcher.find()) {
            int start = matcher.start();
            moduleName = moduleName.substring(0, start);
        }
        // replace non-alphanumeric
        moduleName = NON_ALPHANUM.matcher(moduleName).replaceAll(".");

        // collapse repeating dots
        moduleName = REPEATING_DOTS.matcher(moduleName).replaceAll(".");

        // drop leading dots
        if (moduleName.length() > 0 && moduleName.charAt(0) == '.') {
            moduleName = moduleName.substring(1);
        }

        // drop trailing dots
        int len = moduleName.length();
        if (len > 0 && moduleName.charAt(len - 1) == '.') {
            moduleName = moduleName.substring(0, len - 1);
        }
        return moduleName;
    }

    public static String getModuleName(final File file) throws IOException {
        return getDescriptor(file).name();
    }

}
