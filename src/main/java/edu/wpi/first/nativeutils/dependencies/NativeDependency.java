package edu.wpi.first.nativeutils.dependencies;

import java.util.Optional;

import org.gradle.api.Named;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;

public interface NativeDependency extends Named {
    Optional<ResolvedNativeDependency> resolveNativeDependency(NativePlatform platform, BuildType buildType);

    // default Optional<NativeLibraryBinary> makePrebuilt(NativePlatform platform, BuildType buildType) {
    //     Optional<ResolvedNativeDependency> resolved = resolveNativeDependency(platform, buildType);
    //     if (resolved.isEmpty()) {
    //         return Optional.empty();
    //     }


    // }
}
