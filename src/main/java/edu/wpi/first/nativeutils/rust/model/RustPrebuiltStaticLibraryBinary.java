package edu.wpi.first.nativeutils.rust.model;

import java.io.File;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.Flavor;
import org.gradle.nativeplatform.PrebuiltStaticLibraryBinary;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.OperatingSystemInternal;

import edu.wpi.first.nativeutils.rust.RustVariantConfiguration;

public class RustPrebuiltStaticLibraryBinary implements PrebuiltStaticLibraryBinary {

    private final RustVariantConfiguration configuration;
    private final ObjectFactory objects;
    private final Flavor flavor;
    private final NativePlatform platform;
    private final BuildType buildType;
    private final File staticLibraryFile;

    public RustPrebuiltStaticLibraryBinary(RustVariantConfiguration configuration, NativePlatform platform,
            BuildType buildType, Flavor flavor, ObjectFactory objects) {
        this.configuration = configuration;
        this.flavor = flavor;
        this.platform = platform;
        this.buildType = buildType;
        this.objects = objects;
        OperatingSystemInternal internal = (OperatingSystemInternal) platform.getOperatingSystem();
        staticLibraryFile = configuration.getCargoTask().get().getTargetDirectory().get()
                .file(internal.getInternalOs().getStaticLibraryName(configuration.getArchiveName().get())).getAsFile();
    }

    @Override
    public File getStaticLibraryFile() {
        return staticLibraryFile;
    }

    @Override
    public FileCollection getHeaderDirs() {
        Provider<Directory> headerDir = configuration.getCargoTask().get().getTargetDirectory();
        ConfigurableFileCollection cfc = objects.fileCollection();
        if (headerDir.isPresent()) {
            cfc.builtBy(configuration.getCargoTask());
            cfc.from(headerDir);
        }
        return cfc;
    }

    @Override
    public FileCollection getLinkFiles() {
        ConfigurableFileCollection cfc = objects.fileCollection();
        cfc.builtBy(configuration.getCargoTask());
        cfc.from(getStaticLibraryFile());
        return cfc;
    }

    @Override
    public FileCollection getRuntimeFiles() {
        return objects.fileCollection();
    }

    @Override
    public BuildType getBuildType() {
        return buildType;
    }

    @Override
    public Flavor getFlavor() {
        return flavor;
    }

    @Override
    public NativePlatform getTargetPlatform() {
        return platform;
    }

    @Override
    public String getDisplayName() {
        return "TODO";
    }

    @Override
    public void setStaticLibraryFile(File staticLibraryFile) {
        throw new UnsupportedOperationException("Unimplemented method 'setStaticLibraryFile'");
    }

}
