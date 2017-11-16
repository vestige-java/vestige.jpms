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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Gael Lalire
 */
public class Java9JPMSModuleLayerAccessor implements JPMSModuleLayerAccessor {

    private Java9Controller controller;

    private ModuleLayer moduleLayer;

    protected void setController(final Java9Controller controller) {
        this.controller = controller;
    }

    protected Java9JPMSModuleLayerAccessor(final ModuleLayer moduleLayer) {
        this.moduleLayer = moduleLayer;
        this.controller = Java9JPMSAccessor.MODULE_ENCAPSULATION_BREAKER;
    }

    public Java9JPMSModuleLayerAccessor(final ModuleLayer moduleLayer, final Java9Controller controller) {
        this.moduleLayer = moduleLayer;
        this.controller = controller;
    }

    public ModuleLayer getModuleLayer() {
        return moduleLayer;
    }

    @Override
    public JPMSModuleLayerAccessor defineModules(final ClassLoader parentClassLoader, final List<File> beforeFiles, final List<File> afterFiles, final Collection<String> roots,
            final boolean manyLoaders) {
        Controller controller = Java9JPMSInRepositoryModuleLayerList.defineModules(Collections.singletonList(this), parentClassLoader, beforeFiles, afterFiles, roots, manyLoaders);
        ModuleLayer layer = controller.layer();
        Java9JPMSModuleLayerAccessor layerAccessor = new Java9JPMSModuleLayerAccessor(layer);
        Java9WeakReferenceController<Void> weakController = new Java9WeakReferenceController<Void>(controller);
        layerAccessor.setController(new Java9StrongReferenceController(layer, weakController));
        return layerAccessor;
    }

    @Override
    public JPMSModuleAccessor findModule(final String moduleName) {
        Optional<Module> findModule = moduleLayer.findModule(moduleName);
        if (findModule.isPresent()) {
            return new Java9JPMSModuleAccessor(this, findModule.get());
        }
        return null;
    }

    @Override
    public List<? extends Java9JPMSModuleLayerAccessor> parents() {
        List<ModuleLayer> parents = moduleLayer.parents();
        List<Java9JPMSModuleLayerAccessor> parentsAccessor = new ArrayList<>(parents.size());
        for (ModuleLayer moduleLayer : parents) {
            parentsAccessor.add(new Java9JPMSModuleLayerAccessor(moduleLayer, Java9JPMSAccessor.MODULE_ENCAPSULATION_BREAKER));
        }
        return parentsAccessor;
    }

    public Java9Controller findModuleController(final ModuleLayer otherLayer) {
        for (Java9JPMSModuleLayerAccessor parent : parents()) {
            if (parent.getModuleLayer() == otherLayer) {
                return parent.getController();
            }
            Java9Controller moduleController = parent.findModuleController(otherLayer);
            if (moduleController != null) {
                return moduleController;
            }
        }
        return null;
    }

    @Override
    public Set<JPMSModuleAccessor> modules() {
        Set<Module> modules = moduleLayer.modules();
        Set<JPMSModuleAccessor> modulesAccessor = new HashSet<>();
        for (Module module : modules) {
            modulesAccessor.add(new Java9JPMSModuleAccessor(this, module));
        }
        return modulesAccessor;
    }

    public Java9Controller getController() {
        return controller;
    }

}
