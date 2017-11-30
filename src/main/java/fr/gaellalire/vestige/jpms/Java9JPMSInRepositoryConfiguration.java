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

import java.lang.ModuleLayer.Controller;
import java.util.ArrayList;
import java.util.List;

import fr.gaellalire.vestige.core.function.Function;

/**
 * @author Gael Lalire
 */
public class Java9JPMSInRepositoryConfiguration<ClassLoaderType extends ClassLoader> extends Java9JPMSConfiguration<ClassLoaderType>
        implements JPMSInRepositoryConfiguration<ClassLoaderType> {

    private Java9JPMSModuleLayerRepository repository;

    private List<Java9JPMSInRepositoryModuleLayerAccessor> moduleLayerAccessors;

    private ModuleLayerLinker<ClassLoaderType> linker;

    public Java9JPMSInRepositoryConfiguration(final Java9JPMSModuleLayerRepository repository, final Java9Configuration cf,
            final List<Java9JPMSInRepositoryModuleLayerAccessor> moduleLayerAccessors, final ModuleLayerLinker<ClassLoaderType> linker) {
        super(cf);
        this.repository = repository;
        this.moduleLayerAccessors = moduleLayerAccessors;
        this.linker = linker;
    }

    @Override
    public JPMSInRepositoryModuleLayerAccessor defineModules(final Function<String, ClassLoaderType, RuntimeException> classLoaderByModuleName) {
        final List<ClassLoaderType> definedClassLoaders = new ArrayList<>();
        Controller controller = moduleLayerDefineModules(moduleName -> {
            ClassLoaderType apply = classLoaderByModuleName.apply(moduleName);
            definedClassLoaders.add(apply);
            return apply;
        });
        JPMSInRepositoryModuleLayerAccessor layerAccessor = repository.add(moduleLayerAccessors, controller);
        for (ClassLoaderType classLoader : definedClassLoaders) {
            linker.link(layerAccessor, classLoader);
        }
        return layerAccessor;
    }

    public Java9JPMSModuleLayerRepository getRepository() {
        return repository;
    }

}
