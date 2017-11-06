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
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

/**
 * This class is the bridge to Java Platform Module System. Vestige can't use JPMS function directly because these functions are only in Java 9.
 * @author gaellalire
 */
public class Java9JPMSAccessor implements JPMSAccessor {

    static {
        // force ModuleEncapsulationBreaker init
        try {
            Class.forName(ModuleEncapsulationBreaker.class.getName(), true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new Error("Java9 internal changed");
        }
    }

    /*
    public void defineModulesWithManyLoaders(ClassLoader parentClassLoader, File... files) {

        Path[] paths = new Path[files.length];
        for (int i = 0; i < files.length; i++) {
            paths[i] = files[i].toPath();
        }
        ModuleLayer boot = ModuleLayer.boot();
        Configuration parent = boot.configuration();

        Configuration cf = parent.resolve(ModuleFinder.of(paths), ModuleFinder.of(), Set.of("myapp"));
        boot.defineModulesWithManyLoaders(cf, parentClassLoader);

    }
    */

    @Override
    public JPMSModuleAccessor findBootModule(String moduleName) {
        Optional<Module> findModule = ModuleLayer.boot().findModule(moduleName);
        if (findModule.isPresent()) {
            return new Java9JPMSModuleAccessor(findModule.get());
        }
        return null;
    }

    @Override
    public JPMSModuleAccessor getUnnamedModule(ClassLoader classLoader) {
        return new Java9JPMSModuleAccessor(classLoader.getUnnamedModule());
    }

    @Override
    public JPMSModuleAccessor getModule(Class<?> clazz) {
        return new Java9JPMSModuleAccessor(clazz.getModule());
    }

}
