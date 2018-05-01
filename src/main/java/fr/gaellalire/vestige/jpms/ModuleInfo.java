/*
 * Copyright (c) 2014, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package fr.gaellalire.vestige.jpms;

import static fr.gaellalire.vestige.jpms.ClassFileConstants.ACC_MANDATED;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.ACC_MODULE;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.ACC_OPEN;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.ACC_STATIC_PHASE;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.ACC_SYNTHETIC;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.ACC_TRANSITIVE;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.MODULE;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.MODULE_HASHES;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.MODULE_MAIN_CLASS;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.MODULE_PACKAGES;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.MODULE_RESOLUTION;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.MODULE_TARGET;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.SDE;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.SOURCE_FILE;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.WARN_DEPRECATED;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.WARN_DEPRECATED_FOR_REMOVAL;
import static fr.gaellalire.vestige.jpms.ClassFileConstants.WARN_INCUBATING;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.gaellalire.vestige.jpms.ModuleDescriptor.Builder;
import fr.gaellalire.vestige.jpms.ModuleDescriptor.Exports;
import fr.gaellalire.vestige.jpms.ModuleDescriptor.Opens;
import fr.gaellalire.vestige.jpms.ModuleDescriptor.Requires;

/**
 * Read module information from a {@code module-info} class file.
 * @implNote The rationale for the hand-coded reader is startup performance and fine control over the throwing of InvalidModuleDescriptorException.
 */

public final class ModuleInfo {

    public ModuleInfo() {
    }

    /**
     * Reads the input as a module-info class file.
     * @throws IOException
     * @throws InvalidModuleDescriptorException
     * @throws IllegalArgumentException if thrown by the ModuleDescriptor.Builder because an identifier is not a legal Java identifier, duplicate exports, and many other reasons
     */
    public ModuleDescriptor doRead(final DataInput in) throws IOException {

        int magic = in.readInt();
        if (magic != 0xCAFEBABE) {
            throw invalidModuleDescriptor("Bad magic number");
        }

        // int minor_version =
        in.readUnsignedShort();
        int major_version = in.readUnsignedShort();
        if (major_version < 53) {
            throw invalidModuleDescriptor("Must be >= 53.0");
        }

        ConstantPool cpool = new ConstantPool(in);

        int access_flags = in.readUnsignedShort();
        if (access_flags != ACC_MODULE) {
            throw invalidModuleDescriptor("access_flags should be ACC_MODULE");
        }

        int this_class = in.readUnsignedShort();
        String mn = cpool.getClassName(this_class);
        if (!"module-info".equals(mn)) {
            throw invalidModuleDescriptor("this_class should be module-info");
        }

        int super_class = in.readUnsignedShort();
        if (super_class > 0) {
            throw invalidModuleDescriptor("bad #super_class");
        }

        int interfaces_count = in.readUnsignedShort();
        if (interfaces_count > 0) {
            throw invalidModuleDescriptor("Bad #interfaces");
        }

        int fields_count = in.readUnsignedShort();
        if (fields_count > 0) {
            throw invalidModuleDescriptor("Bad #fields");
        }

        int methods_count = in.readUnsignedShort();
        if (methods_count > 0) {
            throw invalidModuleDescriptor("Bad #methods");
        }

        int attributes_count = in.readUnsignedShort();

        // the names of the attributes found in the class file
        Set<String> attributes = new HashSet<String>();

        Builder builder = null;
        Set<String> allPackages = null;
        String mainClass = null;

        for (int i = 0; i < attributes_count; i++) {
            int name_index = in.readUnsignedShort();
            String attribute_name = cpool.getUtf8(name_index);
            int length = in.readInt();

            boolean added = attributes.add(attribute_name);
            if (!added && isAttributeAtMostOnce(attribute_name)) {
                throw invalidModuleDescriptor("More than one " + attribute_name + " attribute");
            }

            if (MODULE.equals(attribute_name)) {
                builder = readModuleAttribute(in, cpool);
            } else if (MODULE_PACKAGES.equals(attribute_name)) {
                allPackages = readModulePackagesAttribute(in, cpool);
            } else if (MODULE_MAIN_CLASS.equals(attribute_name)) {
                mainClass = readModuleMainClassAttribute(in, cpool);
            } else if (MODULE_TARGET.equals(attribute_name)) {
                readModuleTargetAttribute(in, cpool);
            } else if (MODULE_HASHES.equals(attribute_name)) {
                in.skipBytes(length);
            } else if (MODULE_RESOLUTION.equals(attribute_name)) {
                readModuleResolution(in, cpool);
            } else {
                if (isAttributeDisallowed(attribute_name)) {
                    throw invalidModuleDescriptor(attribute_name + " attribute not allowed");
                } else {
                    in.skipBytes(length);
                }
            }
        }

        // the Module attribute is required
        if (builder == null) {
            throw invalidModuleDescriptor(MODULE + " attribute not found");
        }

        // ModuleMainClass attribute
        if (mainClass != null) {
            builder.mainClass(mainClass);
        }

        // If the ModulePackages attribute is not present then the packageFinder
        // is used to find the set of packages
        boolean usedPackageFinder = false;
        if (allPackages != null) {
            Set<String> knownPackages = builder.packages();
            if (!allPackages.containsAll(knownPackages)) {
                Set<String> missingPackages = new HashSet<String>(knownPackages);
                missingPackages.removeAll(allPackages);
                assert !missingPackages.isEmpty();
                String missingPackage = missingPackages.iterator().next();
                String tail;
                if (usedPackageFinder) {
                    tail = " not found in module";
                } else {
                    tail = " missing from ModulePackages class file attribute";
                }
                throw invalidModuleDescriptor("Package " + missingPackage + tail);

            }
            builder.packages(allPackages);
        }

        ModuleDescriptor descriptor = builder.build();
        return descriptor;
    }

    /**
     * Reads the Module attribute, returning the ModuleDescriptor.Builder to build the corresponding ModuleDescriptor.
     */
    private Builder readModuleAttribute(final DataInput in, final ConstantPool cpool) throws IOException {
        // module_name
        int module_name_index = in.readUnsignedShort();
        String mn = cpool.getModuleName(module_name_index);

        int module_flags = in.readUnsignedShort();

        Set<ModuleDescriptor.Modifier> modifiers = new HashSet<ModuleDescriptor.Modifier>();
        boolean open = ((module_flags & ACC_OPEN) != 0);
        if (open) {
            modifiers.add(ModuleDescriptor.Modifier.OPEN);
        }
        if ((module_flags & ACC_SYNTHETIC) != 0) {
            modifiers.add(ModuleDescriptor.Modifier.SYNTHETIC);
        }
        if ((module_flags & ACC_MANDATED) != 0) {
            modifiers.add(ModuleDescriptor.Modifier.MANDATED);
        }

        Builder builder = new Builder(mn, false, modifiers);

        int module_version_index = in.readUnsignedShort();
        if (module_version_index != 0) {
            String vs = cpool.getUtf8(module_version_index);
            builder.version(vs);
        }

        int requires_count = in.readUnsignedShort();
        boolean requiresJavaBase = false;
        for (int i = 0; i < requires_count; i++) {
            int requires_index = in.readUnsignedShort();
            String dn = cpool.getModuleName(requires_index);

            int requires_flags = in.readUnsignedShort();
            Set<Requires.Modifier> mods;
            if (requires_flags == 0) {
                mods = Collections.emptySet();
            } else {
                mods = new HashSet<Requires.Modifier>();
                if ((requires_flags & ACC_TRANSITIVE) != 0) {
                    mods.add(Requires.Modifier.TRANSITIVE);
                }
                if ((requires_flags & ACC_STATIC_PHASE) != 0) {
                    mods.add(Requires.Modifier.STATIC);
                }
                if ((requires_flags & ACC_SYNTHETIC) != 0) {
                    mods.add(Requires.Modifier.SYNTHETIC);
                }
                if ((requires_flags & ACC_MANDATED) != 0) {
                    mods.add(Requires.Modifier.MANDATED);
                }
            }

            int requires_version_index = in.readUnsignedShort();
            if (requires_version_index == 0) {
                builder.requires(mods, dn);
            } else {
                String vs = cpool.getUtf8(requires_version_index);
                builder.requires(mods, dn, vs);
            }

            if (dn.equals("java.base")) {
                requiresJavaBase = true;
            }
        }
        if (mn.equals("java.base")) {
            if (requires_count > 0) {
                throw invalidModuleDescriptor("The requires table for java.base" + " must be 0 length");
            }
        } else if (!requiresJavaBase) {
            throw invalidModuleDescriptor("The requires table must have" + " an entry for java.base");
        }

        int exports_count = in.readUnsignedShort();
        if (exports_count > 0) {
            for (int i = 0; i < exports_count; i++) {
                int exports_index = in.readUnsignedShort();
                String pkg = cpool.getPackageName(exports_index);

                Set<Exports.Modifier> mods;
                int exports_flags = in.readUnsignedShort();
                if (exports_flags == 0) {
                    mods = Collections.emptySet();
                } else {
                    mods = new HashSet<Exports.Modifier>();
                    if ((exports_flags & ACC_SYNTHETIC) != 0) {
                        mods.add(Exports.Modifier.SYNTHETIC);
                    }
                    if ((exports_flags & ACC_MANDATED) != 0) {
                        mods.add(Exports.Modifier.MANDATED);
                    }
                }

                int exports_to_count = in.readUnsignedShort();
                if (exports_to_count > 0) {
                    Set<String> targets = new HashSet<String>(exports_to_count);
                    for (int j = 0; j < exports_to_count; j++) {
                        int exports_to_index = in.readUnsignedShort();
                        String target = cpool.getModuleName(exports_to_index);
                        if (!targets.add(target)) {
                            throw invalidModuleDescriptor(pkg + " exported to " + target + " more than once");
                        }
                    }
                    builder.exports(mods, pkg, targets);
                } else {
                    builder.exports(mods, pkg);
                }
            }
        }

        int opens_count = in.readUnsignedShort();
        if (opens_count > 0) {
            if (open) {
                throw invalidModuleDescriptor("The opens table for an open" + " module must be 0 length");
            }
            for (int i = 0; i < opens_count; i++) {
                int opens_index = in.readUnsignedShort();
                String pkg = cpool.getPackageName(opens_index);

                Set<Opens.Modifier> mods;
                int opens_flags = in.readUnsignedShort();
                if (opens_flags == 0) {
                    mods = Collections.emptySet();
                } else {
                    mods = new HashSet<Opens.Modifier>();
                    if ((opens_flags & ACC_SYNTHETIC) != 0) {
                        mods.add(Opens.Modifier.SYNTHETIC);
                    }
                    if ((opens_flags & ACC_MANDATED) != 0) {
                        mods.add(Opens.Modifier.MANDATED);
                    }
                }

                int open_to_count = in.readUnsignedShort();
                if (open_to_count > 0) {
                    Set<String> targets = new HashSet<String>(open_to_count);
                    for (int j = 0; j < open_to_count; j++) {
                        int opens_to_index = in.readUnsignedShort();
                        String target = cpool.getModuleName(opens_to_index);
                        if (!targets.add(target)) {
                            throw invalidModuleDescriptor(pkg + " opened to " + target + " more than once");
                        }
                    }
                    builder.opens(mods, pkg, targets);
                } else {
                    builder.opens(mods, pkg);
                }
            }
        }

        int uses_count = in.readUnsignedShort();
        if (uses_count > 0) {
            for (int i = 0; i < uses_count; i++) {
                int index = in.readUnsignedShort();
                String sn = cpool.getClassName(index);
                builder.uses(sn);
            }
        }

        int provides_count = in.readUnsignedShort();
        if (provides_count > 0) {
            for (int i = 0; i < provides_count; i++) {
                int index = in.readUnsignedShort();
                String sn = cpool.getClassName(index);
                int with_count = in.readUnsignedShort();
                List<String> providers = new ArrayList<String>(with_count);
                for (int j = 0; j < with_count; j++) {
                    index = in.readUnsignedShort();
                    String pn = cpool.getClassName(index);
                    if (!providers.add(pn)) {
                        throw invalidModuleDescriptor(sn + " provides " + pn + " more than once");
                    }
                }
                builder.provides(sn, providers);
            }
        }

        return builder;
    }

    /**
     * Reads the ModulePackages attribute
     */
    private Set<String> readModulePackagesAttribute(final DataInput in, final ConstantPool cpool) throws IOException {
        int package_count = in.readUnsignedShort();
        Set<String> packages = new HashSet<String>(package_count);
        for (int i = 0; i < package_count; i++) {
            int index = in.readUnsignedShort();
            String pn = cpool.getPackageName(index);
            boolean added = packages.add(pn);
            if (!added) {
                throw invalidModuleDescriptor("Package " + pn + " in ModulePackages" + "attribute more than once");
            }
        }
        return packages;
    }

    /**
     * Reads the ModuleMainClass attribute
     */
    private String readModuleMainClassAttribute(final DataInput in, final ConstantPool cpool) throws IOException {
        int index = in.readUnsignedShort();
        return cpool.getClassName(index);
    }

    /**
     * Reads the ModuleTarget attribute
     */
    private void readModuleTargetAttribute(final DataInput in, final ConstantPool cpool) throws IOException {

        int index = in.readUnsignedShort();
        if (index != 0) {
            cpool.getUtf8(index);
        }

    }

    /**
     * Reads the ModuleResolution attribute.
     */
    private void readModuleResolution(final DataInput in, final ConstantPool cpool) throws IOException {
        int flags = in.readUnsignedShort();

        int reason = 0;
        if ((flags & WARN_DEPRECATED) != 0) {
            reason = WARN_DEPRECATED;
        }
        if ((flags & WARN_DEPRECATED_FOR_REMOVAL) != 0) {
            if (reason != 0) {
                throw invalidModuleDescriptor("Bad module resolution flags:" + flags);
            }
            reason = WARN_DEPRECATED_FOR_REMOVAL;
        }
        if ((flags & WARN_INCUBATING) != 0) {
            if (reason != 0) {
                throw invalidModuleDescriptor("Bad module resolution flags:" + flags);
            }
        }
    }

    /**
     * Returns true if the given attribute can be present at most once in the class file. Returns false otherwise.
     */
    private static boolean isAttributeAtMostOnce(final String name) {

        if (name.equals(MODULE) || name.equals(SOURCE_FILE) || name.equals(SDE) || name.equals(MODULE_PACKAGES) || name.equals(MODULE_MAIN_CLASS) || name.equals(MODULE_TARGET)
                || name.equals(MODULE_HASHES) || name.equals(MODULE_RESOLUTION)) {
            return true;
        }

        return false;
    }

    /**
     * Return true if the given attribute name is the name of a pre-defined attribute in JVMS 4.7 that is not allowed in a module-info class.
     */
    private static boolean isAttributeDisallowed(final String name) {
        Set<String> notAllowed = predefinedNotAllowed;
        if (notAllowed == null) {
            notAllowed = new HashSet<String>(Arrays.asList("ConstantValue", "Code", "Deprecated", "StackMapTable", "Exceptions", "EnclosingMethod", "Signature", "LineNumberTable",
                    "LocalVariableTable", "LocalVariableTypeTable", "RuntimeVisibleParameterAnnotations", "RuntimeInvisibleParameterAnnotations", "RuntimeVisibleTypeAnnotations",
                    "RuntimeInvisibleTypeAnnotations", "Synthetic", "AnnotationDefault", "BootstrapMethods", "MethodParameters"));
            predefinedNotAllowed = notAllowed;
        }
        return notAllowed.contains(name);
    }

    // lazily created set the pre-defined attributes that are not allowed
    private static volatile Set<String> predefinedNotAllowed;

    /**
     * The constant pool in a class file.
     */
    private static class ConstantPool {
        static final int CONSTANT_Utf8 = 1;

        static final int CONSTANT_Integer = 3;

        static final int CONSTANT_Float = 4;

        static final int CONSTANT_Long = 5;

        static final int CONSTANT_Double = 6;

        static final int CONSTANT_Class = 7;

        static final int CONSTANT_String = 8;

        static final int CONSTANT_Fieldref = 9;

        static final int CONSTANT_Methodref = 10;

        static final int CONSTANT_InterfaceMethodref = 11;

        static final int CONSTANT_NameAndType = 12;

        static final int CONSTANT_MethodHandle = 15;

        static final int CONSTANT_MethodType = 16;

        static final int CONSTANT_InvokeDynamic = 18;

        static final int CONSTANT_Module = 19;

        static final int CONSTANT_Package = 20;

        private static class Entry {
            protected Entry(final int tag) {
                this.tag = tag;
            }

            final int tag;
        }

        private static class IndexEntry extends Entry {
            IndexEntry(final int tag, final int index) {
                super(tag);
                this.index = index;
            }

            final int index;
        }

        private static class Index2Entry extends Entry {
            Index2Entry(final int tag, final int index1, final int index2) {
                super(tag);
            }

        }

        private static class ValueEntry extends Entry {
            ValueEntry(final int tag, final Object value) {
                super(tag);
                this.value = value;
            }

            final Object value;
        }

        final Entry[] pool;

        ConstantPool(final DataInput in) throws IOException {
            int count = in.readUnsignedShort();
            pool = new Entry[count];

            for (int i = 1; i < count; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {

                case CONSTANT_Utf8:
                    String svalue = in.readUTF();
                    pool[i] = new ValueEntry(tag, svalue);
                    break;

                case CONSTANT_Class:
                case CONSTANT_Package:
                case CONSTANT_Module:
                case CONSTANT_String:
                    int index = in.readUnsignedShort();
                    pool[i] = new IndexEntry(tag, index);
                    break;

                case CONSTANT_Double:
                    double dvalue = in.readDouble();
                    pool[i] = new ValueEntry(tag, dvalue);
                    i++;
                    break;

                case CONSTANT_Fieldref:
                case CONSTANT_InterfaceMethodref:
                case CONSTANT_Methodref:
                case CONSTANT_InvokeDynamic:
                case CONSTANT_NameAndType:
                    int index1 = in.readUnsignedShort();
                    int index2 = in.readUnsignedShort();
                    pool[i] = new Index2Entry(tag, index1, index2);
                    break;

                case CONSTANT_MethodHandle:
                    int refKind = in.readUnsignedByte();
                    index = in.readUnsignedShort();
                    pool[i] = new Index2Entry(tag, refKind, index);
                    break;

                case CONSTANT_MethodType:
                    index = in.readUnsignedShort();
                    pool[i] = new IndexEntry(tag, index);
                    break;

                case CONSTANT_Float:
                    float fvalue = in.readFloat();
                    pool[i] = new ValueEntry(tag, fvalue);
                    break;

                case CONSTANT_Integer:
                    int ivalue = in.readInt();
                    pool[i] = new ValueEntry(tag, ivalue);
                    break;

                case CONSTANT_Long:
                    long lvalue = in.readLong();
                    pool[i] = new ValueEntry(tag, lvalue);
                    i++;
                    break;

                default:
                    throw invalidModuleDescriptor("Bad constant pool entry: " + i);
                }
            }
        }

        String getClassName(final int index) {
            checkIndex(index);
            Entry e = pool[index];
            if (e.tag != CONSTANT_Class) {
                throw invalidModuleDescriptor("CONSTANT_Class expected at entry: " + index);
            }
            String value = getUtf8(((IndexEntry) e).index);
            checkUnqualifiedName("CONSTANT_Class", index, value);
            return value.replace('/', '.'); // internal form -> binary name
        }

        String getPackageName(final int index) {
            checkIndex(index);
            Entry e = pool[index];
            if (e.tag != CONSTANT_Package) {
                throw invalidModuleDescriptor("CONSTANT_Package expected at entry: " + index);
            }
            String value = getUtf8(((IndexEntry) e).index);
            checkUnqualifiedName("CONSTANT_Package", index, value);
            return value.replace('/', '.'); // internal form -> binary name
        }

        String getModuleName(final int index) {
            checkIndex(index);
            Entry e = pool[index];
            if (e.tag != CONSTANT_Module) {
                throw invalidModuleDescriptor("CONSTANT_Module expected at entry: " + index);
            }
            String value = getUtf8(((IndexEntry) e).index);
            return decodeModuleName(index, value);
        }

        String getUtf8(final int index) {
            checkIndex(index);
            Entry e = pool[index];
            if (e.tag != CONSTANT_Utf8) {
                throw invalidModuleDescriptor("CONSTANT_Utf8 expected at entry: " + index);
            }
            return (String) (((ValueEntry) e).value);
        }

        void checkIndex(final int index) {
            if (index < 1 || index >= pool.length) {
                throw invalidModuleDescriptor("Index into constant pool out of range");
            }
        }

        void checkUnqualifiedName(final String what, final int index, final String value) {
            int len = value.length();
            if (len == 0) {
                throw invalidModuleDescriptor(what + " at entry " + index + " has zero length");
            }
            for (int i = 0; i < len; i++) {
                char c = value.charAt(i);
                if (c == '.' || c == ';' || c == '[') {
                    throw invalidModuleDescriptor(what + " at entry " + index + " has illegal character: '" + c + "'");
                }
            }
        }

        /**
         * "Decode" a module name that has been read from the constant pool.
         */
        String decodeModuleName(final int index, final String value) {
            int len = value.length();
            if (len == 0) {
                throw invalidModuleDescriptor("CONSTANT_Module at entry " + index + " is zero length");
            }
            int i = 0;
            while (i < len) {
                int cp = value.codePointAt(i);
                if (cp == ':' || cp == '@' || cp < 0x20) {
                    throw invalidModuleDescriptor("CONSTANT_Module at entry " + index + " has illegal character: " + cp);
                }

                // blackslash is the escape character
                if (cp == '\\') {
                    return decodeModuleName(index, i, value);
                }

                i += Character.charCount(cp);
            }
            return value;
        }

        /**
         * "Decode" a module name that has been read from the constant pool and partly checked for illegal characters (up to position {@code i}).
         */
        String decodeModuleName(final int index, int i, final String value) {
            StringBuilder sb = new StringBuilder();

            // copy the code points that have been checked
            int j = 0;
            while (j < i) {
                int cp = value.codePointAt(j);
                sb.appendCodePoint(cp);
                j += Character.charCount(cp);
            }

            // decode from position {@code i} to end
            int len = value.length();
            while (i < len) {
                int cp = value.codePointAt(i);
                if (cp == ':' || cp == '@' || cp < 0x20) {
                    throw invalidModuleDescriptor("CONSTANT_Module at entry " + index + " has illegal character: " + cp);
                }

                // blackslash is the escape character
                if (cp == '\\') {
                    j = i + Character.charCount(cp);
                    if (j >= len) {
                        throw invalidModuleDescriptor("CONSTANT_Module at entry " + index + " has illegal " + "escape sequence");
                    }
                    int next = value.codePointAt(j);
                    if (next != '\\' && next != ':' && next != '@') {
                        throw invalidModuleDescriptor("CONSTANT_Module at entry " + index + " has illegal " + "escape sequence");
                    }
                    sb.appendCodePoint(next);
                    i += Character.charCount(next);
                } else {
                    sb.appendCodePoint(cp);
                }

                i += Character.charCount(cp);
            }
            return sb.toString();
        }
    }

    /**
     * Returns an InvalidModuleDescriptorException with the given detail message
     */
    private static InvalidModuleDescriptorException invalidModuleDescriptor(final String msg) {
        return new InvalidModuleDescriptorException(msg);
    }

}
