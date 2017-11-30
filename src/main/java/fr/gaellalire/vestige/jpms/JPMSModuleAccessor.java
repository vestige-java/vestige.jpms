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

/**
 * @author Gael Lalire
 */
public interface JPMSModuleAccessor {

    void requireBootAddOpens(String sourceModuleName, String packageName);

    void requireBootAddExports(String sourceModuleName, String packageName);

    void addOpens(String packageName, String targetModuleName);

    void addExports(String packageName, String targetModuleName);

    void addOpens(String packageName, Class<?> other);

    void addOpens(String packageName, ClassLoader other);

    void addExports(String packageName, Class<?> other);

    void addExports(String packageName, ClassLoader other);

    void addReads(String targetModuleName);

    void addReads(Class<?> other);

    void addReads(ClassLoader other);

    JPMSModuleLayerAccessor getModuleLayer();

    ClassLoader getClassLoader();

}
