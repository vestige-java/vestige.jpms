
package fr.gaellalire.vestige.jpms;

/**
 * @author Gael Lalire
 */
public class Java9ModuleEncapsulationBreakerProxy implements Java9Controller {

    public void addOpens(final Module module, final String packageName, final Module other) {
        ModuleEncapsulationBreaker.addOpens(module, packageName, other);
    }

    public void addExports(final Module module, final String packageName, final Module other) {
        ModuleEncapsulationBreaker.addExports(module, packageName, other);
    }

    public void addReads(final Module module, final Module other) {
        ModuleEncapsulationBreaker.addReads(module, other);
    }

}
