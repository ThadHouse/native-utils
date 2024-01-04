package edu.wpi.first.nativeutils.model;

import java.io.File;
import java.util.Optional;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.Flavor;
import org.gradle.nativeplatform.SharedLibraryBinary;
import org.gradle.nativeplatform.platform.NativePlatform;

import edu.wpi.first.nativeutils.dependencies.NativeDependency;
import edu.wpi.first.nativeutils.dependencies.ResolvedNativeDependency;

public class WpiRequiredPrebuiltBinary implements SharedLibraryBinary {
    private final NativeDependency library;
    private final BuildType buildType;
    private final NativePlatform targetPlatform;
    private final Flavor flavor;
    private boolean resolved = false;
    private ResolvedNativeDependency fullResolvedDependency;

    public WpiRequiredPrebuiltBinary(NativeDependency library, BuildType buildType,
            NativePlatform targetPlatform, Flavor flavor) {
        this.library = library;
        this.buildType = buildType;
        this.targetPlatform = targetPlatform;
        this.flavor = flavor;
    }

    private void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;

        Optional<ResolvedNativeDependency> resolvedDep = library.resolveNativeDependency(targetPlatform, buildType);
        if (resolvedDep.isEmpty()) {
            throw new GradleException("Missing Dependency " + library.getName());
        }

        fullResolvedDependency = resolvedDep.get();
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
        resolve();
        return fullResolvedDependency.getIncludeRoots();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    @Override
    public FileCollection getLinkFiles() {
        resolve();
        return fullResolvedDependency.getLinkFiles();
    }

    @Override
    public FileCollection getRuntimeFiles() {
        resolve();
        return fullResolvedDependency.getRuntimeFiles();
    }

    @Override
    public String getDisplayName() {
        return "WPI required library '" + getName() + "'";
    }

    @Override
    public File getSharedLibraryFile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSharedLibraryFile'");
    }

    @Override
    public File getSharedLibraryLinkFile() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSharedLibraryLinkFile'");
    }

}
