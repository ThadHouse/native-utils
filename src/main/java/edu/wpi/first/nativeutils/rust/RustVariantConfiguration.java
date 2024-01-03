package edu.wpi.first.nativeutils.rust;

import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public class RustVariantConfiguration {
    private final String buildType;
    private final String targetPlatform;
    private final TaskProvider<CargoBuild> cargoTask;
    private final Provider<String> archiveName;

    public RustVariantConfiguration(String buildType, String targetPlatform, TaskProvider<CargoBuild> cargoTask, Provider<String> archiveName) {
        this.buildType = buildType;
        this.targetPlatform = targetPlatform;
        this.cargoTask = cargoTask;
        this.archiveName = archiveName;
    }

    public String getBuildType() {
        return buildType;
    }

    public String getTargetPlatform() {
        return targetPlatform;
    }

    public TaskProvider<CargoBuild> getCargoTask() {
        return cargoTask;
    }

    public Provider<String> getArchiveName() {
        return archiveName;
    }
}
