/Library/Java/JavaVirtualMachines/jdk-9.0.1.jdk/Contents/Home/bin/javac --patch-module java.base=java_protected -d target/java.base java_protected/java.base/java/lang/ModuleEncapsulationBreaker.java
pushd target/java.base/ && zip -r ../../assembly/moduleEncapsulationBreaker.jar *; popd
rm -rf target
