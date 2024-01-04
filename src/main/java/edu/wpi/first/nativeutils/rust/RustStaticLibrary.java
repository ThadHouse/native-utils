package edu.wpi.first.nativeutils.rust;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.gradle.api.DomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.Flavor;
import org.gradle.nativeplatform.FlavorContainer;
import org.gradle.nativeplatform.NativeLibraryBinary;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.platform.base.Platform;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.rust.model.RustPrebuiltStaticLibraryBinary;

public abstract class RustStaticLibrary extends RustLibrary {

    private final ObjectFactory objects;

    @Inject
    public RustStaticLibrary(String name, RustProject rustProject, ObjectFactory objects, NativeUtilsExtension nue) {
        super(name, rustProject, objects, nue);
        this.objects = objects;
    }

    public abstract Property<String> getArchiveName();

    private final List<RustVariantConfiguration> variants = new ArrayList<>();

    public void addVariant(String platform, String buildType) {
        TaskProvider<CargoBuild> buildTask = getRustProject().getTaskForVariant(platform, buildType);

        variants.add(new RustVariantConfiguration(buildType, platform, buildTask, getArchiveName()));
    }

    @Override
    public void createPrebuiltBinaries(DomainObjectSet<NativeLibraryBinary> binaries, PlatformContainer platforms, BuildTypeContainer buildTypes, FlavorContainer flavors) {
        for (RustVariantConfiguration variant : variants) {
            Platform platform = platforms.findByName(variant.getTargetPlatform());
            if (platform == null || !(platform instanceof NativePlatform)) {
                continue;
            }
            BuildType buildType = buildTypes.findByName(variant.getBuildType());
            if (buildType == null) {
                continue;
            }
            for (Flavor flavor : flavors) {
                RustPrebuiltStaticLibraryBinary binary = new RustPrebuiltStaticLibraryBinary(variant, (NativePlatform)platform, buildType, flavor, objects);
                binaries.add(binary);
            }
        }
    }
}
