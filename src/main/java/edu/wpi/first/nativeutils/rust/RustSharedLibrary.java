package edu.wpi.first.nativeutils.rust;

import javax.inject.Inject;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

public abstract class RustSharedLibrary extends RustLibrary {

    @Inject
    public RustSharedLibrary(String name, RustProject rustProject) {
        super(name, rustProject);
    }

    public abstract Property<String> getArchiveName();

    public void addVariant(String platform, String buildType) {
        TaskProvider<CargoBuild> buildTask = getRustProject().getTaskForVariant(platform, buildType);
    }
}
