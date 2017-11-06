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
 * @author gaellalire
 */
public class Java9JPMSModuleAccessor implements JPMSModuleAccessor {

    private Module module;

    public Java9JPMSModuleAccessor(Module module) {
        this.module = module;
    }

    public boolean addOpens(String packageName, Module other) {
        try {
            ModuleEncapsulationBreaker.addOpens(module, packageName, other);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addExports(String packageName, Module other) {
        try {
            ModuleEncapsulationBreaker.addExports(module, packageName, other);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addReads(Module other) {
        try {
            ModuleEncapsulationBreaker.addReads(module, other);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean addOpens(String packageName, Class<?> other) {
        return addOpens(packageName, other.getModule());
    }

    @Override
    public boolean addOpens(String packageName, ClassLoader other) {
        return addOpens(packageName, other.getUnnamedModule());
    }

    @Override
    public boolean addExports(String packageName, Class<?> other) {
        return addExports(packageName, other.getModule());
    }

    @Override
    public boolean addExports(String packageName, ClassLoader other) {
        return addExports(packageName, other.getUnnamedModule());
    }

    @Override
    public boolean addReads(Class<?> other) {
        return addReads(other.getModule());
    }

    @Override
    public boolean addReads(ClassLoader other) {
        return addReads(other.getUnnamedModule());
    }

}
