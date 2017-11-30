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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Gael Lalire
 */
public class Java9JPMSModuleLayerRepository implements JPMSModuleLayerRepository {

    private List<WeakReference<Java9JPMSInRepositoryModuleLayerAccessor>> moduleLayerAccessors;

    private Java9JPMSInRepositoryModuleLayerAccessor bootLayerAccessor;

    private int firstFree;

    public Java9JPMSModuleLayerRepository() {
        moduleLayerAccessors = new ArrayList<>();
        // in repository we are clean : we don't use the module encapsulation breaker
        bootLayerAccessor = new Java9JPMSInRepositoryModuleLayerAccessor(Collections.emptyList(), ModuleLayer.boot(), Java9Controller.DUMMY_INSTANCE, this, 0);
        firstFree = -1;
    }

    @Override
    public void clean() {
        ListIterator<WeakReference<Java9JPMSInRepositoryModuleLayerAccessor>> iterator = moduleLayerAccessors.listIterator(moduleLayerAccessors.size());
        boolean referenceFound = false;
        int pos = moduleLayerAccessors.size();
        firstFree = pos;
        while (iterator.hasPrevious()) {
            if (iterator.previous().get() == null) {
                if (referenceFound) {
                    if (pos < firstFree) {
                        firstFree = pos;
                    }
                    iterator.set(null);
                } else {
                    iterator.remove();
                }
            } else {
                referenceFound = true;
            }
        }
        if (!referenceFound) {
            firstFree = -1;
        }
    }

    @Override
    public JPMSInRepositoryModuleLayerParentList createModuleLayerList() {
        return new Java9JPMSInRepositoryModuleLayerParentList(this);
    }

    @Override
    public Java9JPMSInRepositoryModuleLayerAccessor get(final int index) {
        if (index == BOOT_LAYER_INDEX) {
            return bootLayerAccessor;
        }
        return moduleLayerAccessors.get(index).get();
    }

    public JPMSInRepositoryModuleLayerAccessor add(final List<Java9JPMSInRepositoryModuleLayerAccessor> parents, final Controller controller) {
        int repositoryIndex;
        boolean add = false;
        if (firstFree == -1) {
            repositoryIndex = moduleLayerAccessors.size();
            add = true;
        } else {
            repositoryIndex = firstFree;
        }
        ModuleLayer layer = controller.layer();
        Java9JPMSInRepositoryModuleLayerAccessor layerAccessor = new Java9JPMSInRepositoryModuleLayerAccessor(parents, layer, new Java9ControllerProxy(controller), this,
                repositoryIndex);
        if (add) {
            moduleLayerAccessors.add(new WeakReference<>(layerAccessor));
        } else {
            moduleLayerAccessors.set(repositoryIndex, new WeakReference<>(layerAccessor));
            clean();
        }
        return layerAccessor;
    }

    @Override
    public int size() {
        return moduleLayerAccessors.size();
    }

}
