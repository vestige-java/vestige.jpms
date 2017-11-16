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

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Gael Lalire
 */
public interface JPMSModuleLayerAccessor {

    JPMSModuleLayerAccessor defineModules(ClassLoader parentClassLoader, List<File> beforeFiles, List<File> afterFiles, Collection<String> roots, boolean manyLoaders);

    JPMSModuleAccessor findModule(String moduleName);

    List<? extends JPMSModuleLayerAccessor> parents();

    Set<? extends JPMSModuleAccessor> modules();

}
