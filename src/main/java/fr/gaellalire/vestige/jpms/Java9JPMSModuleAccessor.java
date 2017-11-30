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

import java.util.Optional;

/**
 * @author Gael Lalire
 */
public class Java9JPMSModuleAccessor implements JPMSModuleAccessor {

    private Module module;

    private Java9JPMSModuleLayerAccessor layerAccessor;

    private Java9Controller controller;

    public Java9JPMSModuleAccessor(final Java9JPMSModuleLayerAccessor layerAccessor, final Module module) {
        this.layerAccessor = layerAccessor;
        if (layerAccessor == null) {
            this.controller = Java9Controller.DUMMY_INSTANCE;
        }
        this.controller = layerAccessor.getController();
        this.module = module;
    }

    public void addOpens(final String packageName, final Module other) {
        controller.addOpens(module, packageName, other);
    }

    public void addExports(final String packageName, final Module other) {
        controller.addExports(module, packageName, other);
    }

    /**
     * If the caller's module is this module then update this module to read the given module. This method is a no-op if {@code other} is this module (all modules read themselves),
     * this module is an unnamed module (as unnamed modules read all modules), or this module already reads {@code other}.
     * @implNote <em>Read edges</em> added by this method are <em>weak</em> and do not prevent {@code other} from being GC'ed when this module is strongly reachable.
     * @param other The other module
     */
    public void addReads(final Module other) {
        controller.addReads(module, other);
    }

    @Override
    public void addOpens(final String packageName, final Class<?> other) {
        addOpens(packageName, other.getModule());
    }

    @Override
    public void addOpens(final String packageName, final ClassLoader other) {
        addOpens(packageName, other.getUnnamedModule());
    }

    @Override
    public void addExports(final String packageName, final Class<?> other) {
        addExports(packageName, other.getModule());
    }

    @Override
    public void addExports(final String packageName, final ClassLoader other) {
        addExports(packageName, other.getUnnamedModule());
    }

    @Override
    public void addReads(final Class<?> other) {
        addReads(other.getModule());
    }

    @Override
    public void addReads(final ClassLoader other) {
        addReads(other.getUnnamedModule());
    }

    @Override
    public void addReads(final String moduleName) {
        if (!module.isNamed()) {
            return;
        }
        ModuleLayer layer = module.getLayer();
        if (layer != null) {
            Optional<Module> other = layer.findModule(moduleName);
            if (other.isPresent()) {
                addReads(other.get());
            }
        }
    }

    @Override
    public Java9JPMSModuleLayerAccessor getModuleLayer() {
        return layerAccessor;
    }

    @Override
    public ClassLoader getClassLoader() {
        return module.getClassLoader();
    }

    public Module findModule(final String moduleName) {
        Optional<Module> findModule = layerAccessor.getModuleLayer().findModule(moduleName);
        if (!findModule.isPresent()) {
            return null;
        }
        return findModule.get();
    }

    @Override
    public void addOpens(final String packageName, final String targetModuleName) {
        Module targetModule = findModule(targetModuleName);
        if (targetModule == null) {
            return;
        }
        controller.addOpens(module, packageName, targetModule);
    }

    @Override
    public void addExports(final String packageName, final String targetModuleName) {
        Module targetModule = findModule(targetModuleName);
        if (targetModule == null) {
            return;
        }
        controller.addExports(module, packageName, targetModule);
    }

    @Override
    public void requireBootAddOpens(final String sourceModuleName, final String packageName) {
        Optional<Module> findModule = ModuleLayer.boot().findModule(sourceModuleName);
        if (findModule.isPresent()) {
            Java9JPMSAccessor.MODULE_ENCAPSULATION_BREAKER.addOpens(findModule.get(), packageName, module);
        }
    }

    @Override
    public void requireBootAddExports(final String sourceModuleName, final String packageName) {
        Optional<Module> findModule = ModuleLayer.boot().findModule(sourceModuleName);
        if (findModule.isPresent()) {
            Java9JPMSAccessor.MODULE_ENCAPSULATION_BREAKER.addExports(findModule.get(), packageName, module);
        }
    }

}
