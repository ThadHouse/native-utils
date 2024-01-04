package edu.wpi.first.nativeutils.model;

import java.io.File;

import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.Flavor;
import org.gradle.nativeplatform.StaticLibraryBinary;
import org.gradle.nativeplatform.platform.NativePlatform;

import edu.wpi.first.nativeutils.dependencies.NativeDependency;

public class WpiOptionalPrebuiltBinary implements StaticLibraryBinary {
    private final NativeDependency library;
    private final BuildType buildType;
    private final NativePlatform targetPlatform;
    private final Flavor flavor;

    public WpiOptionalPrebuiltBinary(NativeDependency library, BuildType buildType,
            NativePlatform targetPlatform, Flavor flavor) {
        this.library = library;
        this.buildType = buildType;
        this.targetPlatform = targetPlatform;
        this.flavor = flavor;
    }

    public String getName() {
        return library.getName();
    }

    @Override
    public BuildType getBuildType() {
        return buildType;
    }

    @Override
    public NativePlatform getTargetPlatform() {
        return targetPlatform;
    }

    @Override
    public Flavor getFlavor() {
        return flavor;
    }

    @Override
    public FileCollection getHeaderDirs() {
        throw new UnsupportedOperationException("Unimplemented method 'getHeaderDirs'");
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public FileCollection getLinkFiles() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getLinkFiles'");
    }

    @Override
    public FileCollection getRuntimeFiles() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getRuntimeFiles'");
    }

    @Override
    public String getDisplayName() {
        return "WPI optional library '" + getName() + "'";
    }

    @Override
    public File getStaticLibraryFile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getStaticLibraryFile'");
    }

}
