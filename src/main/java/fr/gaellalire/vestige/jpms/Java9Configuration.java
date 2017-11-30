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

import java.lang.module.Configuration;
import java.util.List;
import java.util.Set;

import fr.gaellalire.vestige.core.ModuleEncapsulationEnforcer;

/**
 * @author Gael Lalire
 */
public class Java9Configuration {

    private Configuration configuration;

    private List<ModuleLayer> moduleLayers;

    private ModuleEncapsulationEnforcer moduleEncapsulationEnforcer;

    private Set<String> encapsulatedPackageNames;

    public Java9Configuration(final Configuration configuration, final List<ModuleLayer> moduleLayers, final ModuleEncapsulationEnforcer moduleEncapsulationEnforcer,
            final Set<String> encapsulatedPackageNames) {
        this.configuration = configuration;
        this.moduleLayers = moduleLayers;
        this.moduleEncapsulationEnforcer = moduleEncapsulationEnforcer;
        this.encapsulatedPackageNames = encapsulatedPackageNames;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public List<ModuleLayer> getModuleLayers() {
        return moduleLayers;
    }

    public ModuleEncapsulationEnforcer getModuleEncapsulationEnforcer() {
        return moduleEncapsulationEnforcer;
    }

    public Set<String> getEncapsulatedPackageNames() {
        return encapsulatedPackageNames;
    }

}
