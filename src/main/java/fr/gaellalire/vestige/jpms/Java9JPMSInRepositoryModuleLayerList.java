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
import java.lang.ModuleLayer.Controller;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Gael Lalire
 */
public class Java9JPMSInRepositoryModuleLayerList implements JPMSInRepositoryModuleLayerList {

    private List<Java9JPMSInRepositoryModuleLayerAccessor> moduleLayerAccessors = new ArrayList<>();

    private Java9JPMSModuleLayerRepository repository;

    public Java9JPMSInRepositoryModuleLayerList(final Java9JPMSModuleLayerRepository java9jpmsModuleLayerRepository) {
        this.repository = java9jpmsModuleLayerRepository;
    }

    @Override
    public JPMSModuleLayerRepository getRepository() {
        return repository;
    }

    @Override
    public void clear() {
        moduleLayerAccessors.clear();
    }

    @Override
    public void addInRepositoryModuleLayerByIndex(final int index) {
        moduleLayerAccessors.add(repository.get(index));
    }

    public static Controller defineModules(final List<? extends Java9JPMSModuleLayerAccessor> moduleLayerAccessors, final ClassLoader parentClassLoader,
            final List<File> beforeFiles, final List<File> afterFiles, final Collection<String> roots, final boolean manyLoaders) {
        int size = beforeFiles.size();
        Path[] beforePaths = new Path[size];
        for (int i = 0; i < size; i++) {
            beforePaths[i] = beforeFiles.get(i).toPath();
        }
        size = afterFiles.size();
        Path[] afterPaths = new Path[size];
        for (int i = 0; i < size; i++) {
            afterPaths[i] = afterFiles.get(i).toPath();
        }
        ModuleFinder after = ModuleFinder.of(afterPaths);
        ModuleFinder before = ModuleFinder.of(beforePaths);

        List<ModuleLayer> moduleLayers = new ArrayList<>(moduleLayerAccessors.size());
        List<Configuration> configurations = new ArrayList<>(moduleLayerAccessors.size());
        for (Java9JPMSModuleLayerAccessor moduleLayerAccessor : moduleLayerAccessors) {
            configurations.add(moduleLayerAccessor.getModuleLayer().configuration());
            moduleLayers.add(moduleLayerAccessor.getModuleLayer());
        }

        Configuration cf = Configuration.resolve(before, configurations, after, roots);
        Controller controller;
        if (manyLoaders) {
            controller = ModuleLayer.defineModulesWithManyLoaders(cf, moduleLayers, parentClassLoader);
        } else {
            controller = ModuleLayer.defineModulesWithOneLoader(cf, moduleLayers, parentClassLoader);
        }
        return controller;
    }

    @Override
    public JPMSInRepositoryModuleLayerAccessor defineModules(final ClassLoader parentClassLoader, final List<File> beforeFiles, final List<File> afterFiles,
            final Collection<String> roots, final boolean manyLoaders) {
        List<Java9JPMSInRepositoryModuleLayerAccessor> moduleLayerAccessorsCopy = new ArrayList<>(moduleLayerAccessors);
        Controller controller = defineModules(moduleLayerAccessorsCopy, parentClassLoader, beforeFiles, afterFiles, roots, manyLoaders);
        return repository.add(moduleLayerAccessorsCopy, controller);
    }

}
