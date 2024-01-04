package edu.wpi.first.nativeutils.rust;

import javax.inject.Inject;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.FlavorContainer;
import org.gradle.nativeplatform.NativeLibraryBinary;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.WPINativeUtilsExtension.Platforms;
import edu.wpi.first.nativeutils.dependencies.ExtractDelegatedDependencySet;

public abstract class RustLibrary implements Named {
    private final String name;
    private final RustProject rustProject;
    private final ObjectFactory objects;
    private final NativeUtilsExtension nue;

    @Inject
    public RustLibrary(String name, RustProject rustProject, ObjectFactory objects, NativeUtilsExtension nue) {
        this.name = name;
        this.rustProject = rustProject;
        this.objects = objects;
        this.nue = nue;
    }

    public String getName() {
        return name;
    }

    public RustProject getRustProject() {
        return rustProject;
    }

    public abstract void addVariant(String platform, String buildType);

    public void addRequiredNativeDependency(String... libraries) {
        for (String library : libraries) {
            ExtractDelegatedDependencySet dds = objects.newInstance(ExtractDelegatedDependencySet.class, library, nue.getNativeDependencyContainer(), true);
        }
    }

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
