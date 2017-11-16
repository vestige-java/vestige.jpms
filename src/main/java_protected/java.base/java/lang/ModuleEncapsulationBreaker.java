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

package java.lang;

import java.lang.reflect.ReflectPermission;

/**
 * @author Gael Lalire
 */
public class ModuleEncapsulationBreaker {

    public static void addOpens(Module module, String packageName, Module other) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new ReflectPermission("breakModuleEncapsulation"));
        }
        module.implAddOpens(packageName, other);
    }

    public static void addExports(Module module, String packageName, Module other) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new ReflectPermission("breakModuleEncapsulation"));
        }
        module.implAddExports(packageName, other);
    }

    public static void addReads(Module module, Module other) {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new ReflectPermission("breakModuleEncapsulation"));
        }
        module.implAddReads(other);
    }

}
