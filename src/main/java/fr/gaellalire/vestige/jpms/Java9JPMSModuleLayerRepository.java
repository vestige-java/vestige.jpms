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
import java.util.ListIterator;

/**
 * @author Gael Lalire
 */
public class Java9JPMSModuleLayerRepository implements JPMSModuleLayerRepository {

    private List<Java9WeakReferenceController<Java9JPMSInRepositoryModuleLayerAccessor>> controllers;

    public Java9JPMSModuleLayerRepository() {
        controllers = new ArrayList<>();
    }

    public ModuleLayer getModuleLayer(final int index) {
        return controllers.get(index).getModuleLayer();
    }

    @Override
    public void clean() {
        ListIterator<Java9WeakReferenceController<Java9JPMSInRepositoryModuleLayerAccessor>> iterator = controllers.listIterator(controllers.size());
        boolean referenceFound = false;
        while (iterator.hasPrevious()) {
            if (iterator.previous().getModuleLayer() == null) {
                if (referenceFound) {
                    iterator.set(null);
                } else {
                    iterator.remove();
                }
            } else {
                referenceFound = true;
            }
        }
    }

    @Override
    public JPMSInRepositoryModuleLayerList createModuleLayerList() {
        return new Java9JPMSInRepositoryModuleLayerList(this);
    }

    public List<Java9JPMSInRepositoryModuleLayerAccessor> restoreParents(final List<ModuleLayer> moduleLayerParents) {
        List<Java9JPMSInRepositoryModuleLayerAccessor> parents = new ArrayList<>();
        for (ModuleLayer moduleLayerParent : moduleLayerParents) {
            boolean found = false;
            for (int i = 0; i < controllers.size(); i++) {
                Java9WeakReferenceController<Java9JPMSInRepositoryModuleLayerAccessor> controller = controllers.get(i);
                if (controller.getModuleLayer() == moduleLayerParent) {
                    Java9JPMSInRepositoryModuleLayerAccessor layerAccessor = controller.getLayerAccessor();
                    if (layerAccessor == null) {
                        // restore
                        layerAccessor = new Java9JPMSInRepositoryModuleLayerAccessor(restoreParents(moduleLayerParent.parents()), moduleLayerParent, i);
                        controller.setLayerAccessor(layerAccessor);
                    }
                    parents.add(layerAccessor);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // the parent is not in repository, seems impossible
                throw new RuntimeException("Impossible is possible");
            }
        }
        return parents;
    }

    @Override
    public Java9JPMSInRepositoryModuleLayerAccessor get(final int index) {
        Java9WeakReferenceController<Java9JPMSInRepositoryModuleLayerAccessor> weakController = controllers.get(index);
        ModuleLayer moduleLayer = weakController.getModuleLayer();
        if (moduleLayer == null) {
            controllers.set(index, null);
            clean();
            return null;
        }
        Java9JPMSInRepositoryModuleLayerAccessor layerAccessor = weakController.getLayerAccessor();
        if (layerAccessor == null) {
            // reconstruct, it is costly that why we keep layer accessor in soft reference
            layerAccessor = new Java9JPMSInRepositoryModuleLayerAccessor(restoreParents(weakController.getModuleLayer().parents()), moduleLayer, index);
            weakController.setLayerAccessor(layerAccessor);
        }
        return layerAccessor;
    }

    public JPMSInRepositoryModuleLayerAccessor add(final List<Java9JPMSInRepositoryModuleLayerAccessor> parents, final Controller controller) {
        int repositoryIndex = controllers.size();
        ModuleLayer layer = controller.layer();
        Java9JPMSInRepositoryModuleLayerAccessor layerAccessor = new Java9JPMSInRepositoryModuleLayerAccessor(parents, layer, repositoryIndex);
        Java9WeakReferenceController<Java9JPMSInRepositoryModuleLayerAccessor> weakController = new Java9WeakReferenceController<>(controller, layerAccessor);
        layerAccessor.setController(new Java9StrongReferenceController(layer, weakController));
        controllers.add(weakController);
        return layerAccessor;
    }

}
