package edu.wpi.first.nativeutils.rust;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RustPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        project.getExtensions().create("nativeUtilsRust", RustExtension.class);

        project.getPluginManager().apply(RustRules.class);
    }
}
