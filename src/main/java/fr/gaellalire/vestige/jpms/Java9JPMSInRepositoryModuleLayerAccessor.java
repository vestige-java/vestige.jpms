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

import java.util.List;
import java.util.Optional;

/**
 * @author Gael Lalire
 */
public class Java9JPMSInRepositoryModuleLayerAccessor extends Java9JPMSModuleLayerAccessor implements JPMSInRepositoryModuleLayerAccessor {

    private List<Java9JPMSInRepositoryModuleLayerAccessor> parents;

    private Java9JPMSModuleLayerRepository repository;

    private int repositoryIndex;

    public Java9JPMSInRepositoryModuleLayerAccessor(final List<Java9JPMSInRepositoryModuleLayerAccessor> parents, final ModuleLayer moduleLayer, final Java9Controller controller,
            final Java9JPMSModuleLayerRepository repository, final int repositoryIndex) {
        super(moduleLayer, controller);
        this.parents = parents;
        this.repository = repository;
        this.repositoryIndex = repositoryIndex;
    }

    @Override
    public Java9JPMSModuleAccessor findModule(final String moduleName) {
        ModuleLayer moduleLayer = getModuleLayer();
        Optional<Module> findModule = moduleLayer.findModule(moduleName);
        if (findModule.isPresent()) {
            Module module = findModule.get();
            ModuleLayer otherLayer = module.getLayer();
            if (otherLayer == moduleLayer) {
                return new Java9JPMSModuleAccessor(this, module);
            }
            return new Java9JPMSModuleAccessor(findModuleAccessor(otherLayer), module);
        }
        return null;
    }

    public Java9JPMSInRepositoryModuleLayerAccessor findModuleAccessor(final ModuleLayer otherLayer) {
        for (Java9JPMSInRepositoryModuleLayerAccessor parent : parents()) {
            if (parent.getModuleLayer() == otherLayer) {
                return parent;
            }
            Java9JPMSInRepositoryModuleLayerAccessor layerAccessor = parent.findModuleAccessor(otherLayer);
            if (layerAccessor != null) {
                return layerAccessor;
            }
        }
        return null;
    }

    @Override
    public List<? extends Java9JPMSInRepositoryModuleLayerAccessor> parents() {
        return parents;
    }

    @Override
    public int getRepositoryIndex() {
        return repositoryIndex;
    }

    @Override
    public JPMSModuleLayerRepository getRepository() {
        return repository;
    }

}
