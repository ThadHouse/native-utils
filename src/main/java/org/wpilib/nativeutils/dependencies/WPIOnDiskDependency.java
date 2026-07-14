package org.wpilib.nativeutils.dependencies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.MapProperty;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.platform.NativePlatform;

/**
 * A native dependency whose files are already available on disk.
 *
 * <p>The keys in {@link #getBuildDependencies()} and
 * {@link #getRuntimeDependencies()} are native platform names. Platforms not
 * present in either map are treated as header-only platforms. If both maps
 * are empty, the dependency is header-only on every platform.</p>
 */
public abstract class WPIOnDiskDependency implements NativeDependency {
    private final String name;
    private final Project project;

    private final Map<NativePlatform, Map<BuildType, Optional<ResolvedNativeDependency>>> resolvedDependencies = new HashMap<>();

    @Inject
    public WPIOnDiskDependency(String name, Project project) {
        this.name = name;
        this.project = project;
        getBuildDependencies().convention(Map.of());
        getRuntimeDependencies().convention(Map.of());
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * The directory containing the dependency's headers.
     */
    public abstract DirectoryProperty getHeaders();

    /**
     * Platform names mapped to files needed while linking the dependency.
     */
    public abstract MapProperty<String, List<String>> getBuildDependencies();

    /**
     * Platform names mapped to files that must be available when running the
     * application.
     */
    public abstract MapProperty<String, List<String>> getRuntimeDependencies();

    @Override
    public Optional<ResolvedNativeDependency> resolveNativeDependency(NativePlatform platform, BuildType buildType,
            Optional<FastDownloadDependencySet> loaderDependencySet) {
        Map<BuildType, Optional<ResolvedNativeDependency>> buildTypeMap = resolvedDependencies.get(platform);
        if (buildTypeMap != null && buildTypeMap.containsKey(buildType)) {
            return buildTypeMap.get(buildType);
        }

        Map<String, List<String>> buildDependencies = getBuildDependencies().get();
        Map<String, List<String>> runtimeDependencies = getRuntimeDependencies().get();
        String platformName = platform.getName();

        FileCollection headers = getHeaders().isPresent() ? project.files(getHeaders()) : project.files();
        FileCollection sources = project.files();
        FileCollection linkFiles = project.files(buildDependencies.getOrDefault(platformName, List.of()));
        FileCollection runtimeFiles = project.files(runtimeDependencies.getOrDefault(platformName, List.of()));

        Optional<ResolvedNativeDependency> resolvedDependency = Optional
                .of(new ResolvedNativeDependency(headers, sources, linkFiles, runtimeFiles));
        if (buildTypeMap == null) {
            buildTypeMap = new HashMap<>();
            resolvedDependencies.put(platform, buildTypeMap);
        }
        buildTypeMap.put(buildType, resolvedDependency);
        return resolvedDependency;
    }
}
