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
import java.util.Set;

import fr.gaellalire.vestige.core.ModuleEncapsulationEnforcer;
import fr.gaellalire.vestige.core.function.Function;

/**
 * @author Gael Lalire
 */
public class Java9JPMSConfiguration<ClassLoaderType extends ClassLoader> implements JPMSConfiguration<ClassLoaderType> {

    private Java9Configuration cf;

    public Java9JPMSConfiguration(final Java9Configuration cf) {
        this.cf = cf;
    }

    @Override
    public Set<String> getEncapsulatedPackageNames() {
        return cf.getEncapsulatedPackageNames();
    }

    @Override
    public ModuleEncapsulationEnforcer getModuleEncapsulationEnforcer() {
        return cf.getModuleEncapsulationEnforcer();
    }

    public Controller moduleLayerDefineModules(final java.util.function.Function<String, ClassLoader> clf) {
        return ModuleLayer.defineModules(cf.getConfiguration(), cf.getModuleLayers(), clf);
    }

    @Override
    public JPMSModuleLayerAccessor defineModules(final Function<String, ClassLoaderType, RuntimeException> classLoaderByModuleName) {
        Controller controller = moduleLayerDefineModules(moduleName -> classLoaderByModuleName.apply(moduleName));
        return new Java9JPMSModuleLayerAccessor(controller.layer(), new Java9ControllerProxy(controller));
    }

}
