package edu.wpi.first.nativeutils.rust;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.FlavorContainer;
import org.gradle.nativeplatform.NativeLibraryBinary;
import org.gradle.nativeplatform.PrebuiltLibrary;
import org.gradle.platform.base.PlatformContainer;

public class RustPrebuiltLibrary implements PrebuiltLibrary {
    private final RustLibrary library;
    private final SourceDirectorySet headers;
    private final DomainObjectSet<NativeLibraryBinary> binaries;

    public RustPrebuiltLibrary(RustLibrary library, ObjectFactory objects, PlatformContainer platforms, BuildTypeContainer buildTypes, FlavorContainer flavors) {
        this.library = library;
        headers = objects.sourceDirectorySet("headers", "headers for prebuilt library '" + library.getName() + "'");
        binaries = objects.domainObjectSet(NativeLibraryBinary.class);

        library.createPrebuiltBinaries(binaries, platforms, buildTypes, flavors);
    }

    @Override
    public String getName() {
        return library.getName();
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
