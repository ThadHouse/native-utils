package edu.wpi.first.nativeutils.rust;

import javax.inject.Inject;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.FlavorContainer;
import org.gradle.nativeplatform.NativeLibraryBinary;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.nativeutils.WPINativeUtilsExtension.Platforms;

public abstract class RustLibrary implements Named {
    private final String name;
    private final RustProject rustProject;

    @Inject
    public RustLibrary(String name, RustProject rustProject) {
        this.name = name;
        this.rustProject = rustProject;
    }

    public String getName() {
        return name;
    }

    public RustProject getRustProject() {
        return rustProject;
    }

    public abstract void addVariant(String platform, String buildType);

    public void addVariant(NativePlatform platform, BuildType buildType) {
        addVariant(platform.getName(), buildType.getName());
    }

    public void addAllVariants(Platforms platforms) {
        for (String platform : platforms.allPlatforms) {
            addVariant(platform, "release");
            addVariant(platform, "debug");
        }
    }

    public abstract void createPrebuiltBinaries(DomainObjectSet<NativeLibraryBinary> binaries, PlatformContainer platforms, BuildTypeContainer buildTypes, FlavorContainer flavors);
}
