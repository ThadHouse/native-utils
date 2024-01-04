package edu.wpi.first.nativeutils.rust;

import javax.inject.Inject;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskProvider;

import edu.wpi.first.nativeutils.NativeUtilsExtension;

public abstract class RustSharedLibrary extends RustLibrary {

    @Inject
    public RustSharedLibrary(String name, RustProject rustProject, ObjectFactory objects, NativeUtilsExtension nue) {
        super(name, rustProject, objects, nue);
    }

    public abstract Property<String> getArchiveName();

    public void addVariant(String platform, String buildType) {
        TaskProvider<CargoBuild> buildTask = getRustProject().getTaskForVariant(platform, buildType);
    }
}
