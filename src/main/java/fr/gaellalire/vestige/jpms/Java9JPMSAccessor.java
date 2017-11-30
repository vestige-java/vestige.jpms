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

/**
 * This class is the bridge to Java Platform Module System. Vestige can't use JPMS function directly because these functions are only in Java 9.
 * @author Gael Lalire
 */
public class Java9JPMSAccessor implements JPMSAccessor {

    public static final Java9Controller MODULE_ENCAPSULATION_BREAKER;

    static {
        Java9Controller moduleEncapsulationBreakerInterface;
        try {
            moduleEncapsulationBreakerInterface = new Java9ModuleEncapsulationBreakerProxy();
            Module selfModule = Java9JPMSModuleAccessor.class.getModule();
            moduleEncapsulationBreakerInterface.addReads(selfModule, selfModule);
        } catch (Throwable e) {
            // ok to fail, the module encapsulation breaker is broken or not present
            moduleEncapsulationBreakerInterface = Java9Controller.DUMMY_INSTANCE;
        }
        MODULE_ENCAPSULATION_BREAKER = moduleEncapsulationBreakerInterface;
    }

    @Override
    public JPMSModuleLayerAccessor bootLayer() {
        return new Java9JPMSModuleLayerAccessor(ModuleLayer.boot(), Java9JPMSAccessor.MODULE_ENCAPSULATION_BREAKER);
    }

    @Override
    public JPMSModuleAccessor getUnnamedModule(final ClassLoader classLoader) {
        return new Java9JPMSModuleAccessor(null, classLoader.getUnnamedModule());
    }

    @Override
    public JPMSModuleAccessor getModule(final Class<?> clazz) {
        Module module = clazz.getModule();
        ModuleLayer layer = module.getLayer();
        Java9JPMSModuleLayerAccessor layerAccessor = null;
        if (layer != null) {
            layerAccessor = new Java9JPMSModuleLayerAccessor(layer, MODULE_ENCAPSULATION_BREAKER);
        }
        return new Java9JPMSModuleAccessor(layerAccessor, module);
    }

    @Override
    public JPMSModuleLayerRepository createModuleLayerRepository() {
        return new Java9JPMSModuleLayerRepository();
    }

}
