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
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.gaellalire.vestige.core.JPMSVestige;
import fr.gaellalire.vestige.core.ModuleEncapsulationEnforcer;
import fr.gaellalire.vestige.core.parser.ListIndexStringParser;

/**
 * @author Gael Lalire
 */
public class Java9JPMSInRepositoryModuleLayerParentList implements JPMSInRepositoryModuleLayerParentList {

    private List<Java9JPMSInRepositoryModuleLayerAccessor> moduleLayerAccessors = new ArrayList<>();

    private Java9JPMSModuleLayerRepository repository;

    public Java9JPMSInRepositoryModuleLayerParentList(final Java9JPMSModuleLayerRepository java9jpmsModuleLayerRepository) {
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

    public static Java9Configuration createConfiguration(final List<? extends Java9JPMSModuleLayerAccessor> moduleLayerAccessors, final List<File> beforeFiles,
            final List<File> afterFiles, final Collection<String> roots) throws IOException {
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

        Collection<String> resolvedRoots = roots;
        if (resolvedRoots == null) {
            final Collection<String> resolvingRoots = new ArrayList<>();
            after.findAll().stream().forEach(mr -> resolvingRoots.add(mr.descriptor().name()));
            before.findAll().stream().forEach(mr -> resolvingRoots.add(mr.descriptor().name()));
            resolvedRoots = resolvingRoots;
        }

        List<Configuration> configurations = new ArrayList<>(moduleLayerAccessors.size());
        List<ModuleLayer> moduleLayers = new ArrayList<>(moduleLayerAccessors.size());
        for (Java9JPMSModuleLayerAccessor moduleLayerAccessor : moduleLayerAccessors) {
            configurations.add(moduleLayerAccessor.getModuleLayer().configuration());
            moduleLayers.add(moduleLayerAccessor.getModuleLayer());
        }

        Configuration configuration = Configuration.resolve(before, configurations, after, resolvedRoots);

        Map<String, String> moduleNameByPackageName = new HashMap<>();
        Set<String> encapsulatedPackageNames = new HashSet<>();
        Map<File, String> moduleNamesByFile = new HashMap<>();
        List<String> moduleNames = new ArrayList<>(beforeFiles.size() + afterFiles.size());

        JPMSVestige.createEnforcerConfiguration(moduleNamesByFile, moduleNameByPackageName, encapsulatedPackageNames, configuration);
        for (File file : beforeFiles) {
            moduleNames.add(moduleNamesByFile.get(file.getAbsoluteFile()));
        }
        for (File file : afterFiles) {
            moduleNames.add(moduleNamesByFile.get(file.getAbsoluteFile()));
        }
        ModuleEncapsulationEnforcer moduleEncapsulationEnforcer = new ModuleEncapsulationEnforcer(moduleNameByPackageName, new ListIndexStringParser(moduleNames, -2), null);

        return new Java9Configuration(configuration, moduleLayers, moduleEncapsulationEnforcer, encapsulatedPackageNames);
    }

    public <ClassLoaderType extends ClassLoader> JPMSInRepositoryConfiguration<ClassLoaderType> createConfiguration(final List<File> beforeFiles, final List<File> afterFiles,
            final Collection<String> roots, final ModuleLayerLinker<ClassLoaderType> linker) throws IOException {
        List<Java9JPMSInRepositoryModuleLayerAccessor> moduleLayerAccessors = new ArrayList<>(this.moduleLayerAccessors);

        Java9Configuration cf = createConfiguration(moduleLayerAccessors, beforeFiles, afterFiles, roots);

        return new Java9JPMSInRepositoryConfiguration<ClassLoaderType>(repository, cf, moduleLayerAccessors, linker);
    }

}
