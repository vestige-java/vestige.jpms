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
public final class Java9StrongReferenceController implements Java9Controller {

    private ModuleLayer moduleLayer;

    private Java9WeakReferenceController<?> weakController;

    public Java9StrongReferenceController(final ModuleLayer moduleLayer, final Java9WeakReferenceController<?> weakController) {
        this.moduleLayer = moduleLayer;
        this.weakController = weakController;
    }

    public ModuleLayer layer() {
        return moduleLayer;
    }

    public void addReads(final Module source, final Module target) {
        weakController.addReads(source, target);
    }

    public void addOpens(final Module source, final String pn, final Module target) {
        weakController.addOpens(source, pn, target);
    }

    public void addExports(final Module source, final String pn, final Module target) {
        weakController.addExports(source, pn, target);
    }

}
