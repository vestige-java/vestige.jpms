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

/**
 * @author Gael Lalire
 */
public class Java9ControllerProxy implements Java9Controller {

    private Controller controller;

    public Java9ControllerProxy(final Controller controller) {
        this.controller = controller;
    }

    @Override
    public void addOpens(final Module source, final String pn, final Module target) {
        controller.addOpens(source, pn, target);
    }

    @Override
    public void addExports(final Module source, final String pn, final Module target) {
        controller.addExports(source, pn, target);
    }

    @Override
    public void addReads(final Module source, final Module target) {
        controller.addReads(source, target);
    }

    public Controller getController() {
        return controller;
    }

}
