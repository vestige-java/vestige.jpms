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
public final class JPMSAccessorLoader {

    public static final JPMSAccessor INSTANCE;

    static {
        JPMSAccessor java9JPMS = null;
        try {
            @SuppressWarnings("unchecked")
            Class<? extends JPMSAccessor> java9JPMSAccessorClass = (Class<? extends JPMSAccessor>) Class.forName("fr.gaellalire.vestige.jpms.Java9JPMSAccessor", true,
                    Thread.currentThread().getContextClassLoader());
            java9JPMS = java9JPMSAccessorClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            // it is ok to fail, we are not on a JDK 9+
        }
        if (java9JPMS != null) {
            INSTANCE = java9JPMS;
        } else {
            INSTANCE = null;
        }
    }

    public static JPMSAccessor loadWithController(final Object controller) {
        JPMSAccessor java9JPMS = null;
        try {
            @SuppressWarnings("unchecked")
            Class<? extends JPMSAccessor> java9JPMSAccessorClass = (Class<? extends JPMSAccessor>) Class.forName("fr.gaellalire.vestige.jpms.Java9JPMSAccessor", true,
                    Thread.currentThread().getContextClassLoader());
            java9JPMS = java9JPMSAccessorClass.getDeclaredConstructor(Object.class).newInstance(controller);
        } catch (Throwable e) {
            // it is ok to fail, we are not on a JDK 9+
        }
        return java9JPMS;
    }

}
