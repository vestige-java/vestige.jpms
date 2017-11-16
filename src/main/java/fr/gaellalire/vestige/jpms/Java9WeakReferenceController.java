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
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

/**
 * @author Gael Lalire
 */
public final class Java9WeakReferenceController<LayerAccessor> implements Java9Controller {

    private static final Field LAYER_IN_CONTROLLER;

    static {
        Field field;
        try {
            JPMSAccessor instance = JPMSAccessorLoader.INSTANCE;
            instance.bootLayer().findModule("java.base").addOpens("java.lang", Java9WeakReferenceController.class);
            field = Controller.class.getDeclaredField("layer");
            field.setAccessible(true);
        } catch (Throwable e) {
            // sad we will have full memory leak
            field = null;
        }
        LAYER_IN_CONTROLLER = field;
    }

    private Controller controller;

    private WeakReference<ModuleLayer> layerWeakReference;

    private SoftReference<LayerAccessor> layerAccessorCache;

    Java9WeakReferenceController(final Controller controller) {
        this.controller = controller;
        this.layerWeakReference = new WeakReference<>(controller.layer());
    }

    Java9WeakReferenceController(final Controller controller, final LayerAccessor java9jpmsInRepositoryModuleLayerAccessor) {
        this.controller = controller;
        this.layerWeakReference = new WeakReference<>(controller.layer());
        this.layerAccessorCache = new SoftReference<>(java9jpmsInRepositoryModuleLayerAccessor);
    }

    public ModuleLayer getModuleLayer() {
        return layerWeakReference.get();
    }

    public boolean restore() {
        if (LAYER_IN_CONTROLLER == null) {
            // never been cleaned
            return true;
        }
        ModuleLayer moduleLayer = layerWeakReference.get();
        if (moduleLayer == null) {
            return false;
        }
        try {
            LAYER_IN_CONTROLLER.set(controller, moduleLayer);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            // not normal
        }
        return true;
    }

    public final void clean() {
        if (LAYER_IN_CONTROLLER != null) {
            try {
                LAYER_IN_CONTROLLER.set(controller, null);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                // not normal
            }
        }
    }

    public synchronized void addReads(final Module source, final Module target) {
        if (!restore()) {
            return;
        }
        try {
            controller.addReads(source, target);
        } finally {
            clean();
        }
    }

    public synchronized void addOpens(final Module source, final String pn, final Module target) {
        if (!restore()) {
            return;
        }
        try {
            controller.addOpens(source, pn, target);
        } finally {
            clean();
        }
    }

    public synchronized void addExports(final Module source, final String pn, final Module target) {
        if (!restore()) {
            return;
        }
        try {
            controller.addExports(source, pn, target);
        } finally {
            clean();
        }
    }

    public void setLayerAccessor(final LayerAccessor layerAccessor) {
        layerAccessorCache = new SoftReference<>(layerAccessor);
    }

    public LayerAccessor getLayerAccessor() {
        return layerAccessorCache.get();
    }

}
