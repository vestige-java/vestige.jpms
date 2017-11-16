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
 * @author Gael Lalire
 */
public interface Java9Controller {

    Java9Controller DUMMY_INSTANCE = new Java9Controller() {

        @Override
        public void addOpens(final Module module, final String packageName, final Module other) {
        }

        @Override
        public void addExports(final Module module, final String packageName, final Module other) {
        }

        @Override
        public void addReads(final Module module, final Module other) {
        }

    };

    /**
     * Updates module {@code source} in the layer to open a package to module {@code target}. This method is a no-op if {@code source} already opens the package to at least
     * {@code target}.
     * @param source The source module
     * @param pn The package name
     * @param target The target module
     * @return This controller
     * @throws IllegalArgumentException If {@code source} is not in the module layer or the package is not in the source module
     * @see Module#addOpens
     */
    void addOpens(Module source, String pn, Module target);

    /**
     * Updates module {@code source} in the layer to export a package to module {@code target}. This method is a no-op if {@code source} already exports the package to at least
     * {@code target}.
     * @param source The source module
     * @param pn The package name
     * @param target The target module
     * @return This controller
     * @throws IllegalArgumentException If {@code source} is not in the module layer or the package is not in the source module
     * @see Module#addExports
     */
    void addExports(Module source, String pn, Module target);

    /**
     * Updates module {@code source} in the layer to read module {@code target}. This method is a no-op if {@code source} already reads {@code target}.
     * @implNote <em>Read edges</em> added by this method are <em>weak</em> and do not prevent {@code target} from being GC'ed when {@code source} is strongly reachable.
     * @param source The source module
     * @param target The target module to read
     * @return This controller
     * @throws IllegalArgumentException If {@code source} is not in the module layer
     * @see Module#addReads
     */
    void addReads(Module source, Module target);

}
