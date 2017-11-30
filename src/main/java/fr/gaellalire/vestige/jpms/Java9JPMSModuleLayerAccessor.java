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

    public Java9JPMSModuleLayerAccessor(final ModuleLayer moduleLayer, final Java9Controller controller) {
        this.moduleLayer = moduleLayer;
        this.controller = controller;
    }

    public ModuleLayer getModuleLayer() {
        return moduleLayer;
    }

    @Override
    public JPMSModuleAccessor findModule(final String moduleName) {
        Optional<Module> findModule = moduleLayer.findModule(moduleName);
        if (findModule.isPresent()) {
            Module module = findModule.get();
            ModuleLayer otherLayer = module.getLayer();
            if (otherLayer == moduleLayer) {
                return new Java9JPMSModuleAccessor(this, module);
            }
            return new Java9JPMSModuleAccessor(new Java9JPMSModuleLayerAccessor(otherLayer, Java9JPMSAccessor.MODULE_ENCAPSULATION_BREAKER), module);
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

    @Override
    public JPMSConfiguration<ClassLoader> createConfiguration(final List<File> beforeFiles, final List<File> afterFiles, final Collection<String> roots) throws IOException {
        return new Java9JPMSConfiguration<ClassLoader>(
                Java9JPMSInRepositoryModuleLayerParentList.createConfiguration(Collections.singletonList(this), beforeFiles, afterFiles, roots));
    }

}
