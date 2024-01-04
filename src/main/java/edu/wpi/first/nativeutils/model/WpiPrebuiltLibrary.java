package edu.wpi.first.nativeutils.model;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.NativeLibraryBinary;
import org.gradle.nativeplatform.PrebuiltLibrary;

public class WpiPrebuiltLibrary implements PrebuiltLibrary {
    private final String name;
    private final SourceDirectorySet headers;
    private final DomainObjectSet<NativeLibraryBinary> binaries;

    public WpiPrebuiltLibrary(String name, ObjectFactory objects) {
        this.name = name;
        headers = objects.sourceDirectorySet("headers", "headers for prebuilt library '" + name + "'");
        binaries = objects.domainObjectSet(NativeLibraryBinary.class);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DomainObjectSet<NativeLibraryBinary> getBinaries() {
        return binaries;
    }

    @Override
    public SourceDirectorySet getHeaders() {
        return headers;
    }

}
