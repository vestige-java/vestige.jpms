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
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author Gael Lalire
 */
public interface JPMSInRepositoryModuleLayerParentList {

    JPMSModuleLayerRepository getRepository();

    void clear();

    void addInRepositoryModuleLayerByIndex(int index);

    <ClassLoaderType extends ClassLoader> JPMSInRepositoryConfiguration<ClassLoaderType> createConfiguration(List<File> beforeFiles, List<File> afterFiles,
            Collection<String> roots, ModuleLayerLinker<ClassLoaderType> linker) throws IOException;

}
