package edu.wpi.first.nativeutils.rust;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.gradle.api.file.ProjectLayout;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

public class RustProject {
    private final TaskContainer tasks;
    private final CargoLocator locator;
    private final ProjectLayout layout;

    private final Map<String, Map<String, TaskProvider<CargoBuild>>> buildTasks = new HashMap<>();

    private Map<String, TaskProvider<CargoBuild>> getInnerMap(String platform) {
        Map<String, TaskProvider<CargoBuild>> buildTypeMap = buildTasks.get(platform);

        if (buildTypeMap == null) {
            buildTypeMap = new HashMap<>();
            buildTasks.put(platform, buildTypeMap);
        }

        return buildTypeMap;
    }

    private void addToCache(String platform, String buildType, TaskProvider<CargoBuild> dependency) {
        Map<String, TaskProvider<CargoBuild>> innerMap = getInnerMap(platform);
        innerMap.put(buildType, dependency);
    }

    private TaskProvider<CargoBuild> tryFromCache(String platform, String buildType) {
        Map<String, TaskProvider<CargoBuild>> innerMap = getInnerMap(platform);
        return innerMap.getOrDefault(buildType, null);
    }

    @Inject
    public RustProject(TaskContainer tasks, CargoLocator locator, ProjectLayout layout) {
        this.tasks = tasks;
        this.locator = locator;
        this.layout = layout;
    }

    public Map<String, Map<String, TaskProvider<CargoBuild>>> getBuildTasks() {
        return buildTasks;
    }

    private final Map<String, String> tripleMap = new HashMap<>(Map.of("linuxathena", "armv7-unknown-linux-gnueabi", "windowsx86-64", "x86_64-pc-windows-msvc", "windowsarm64", "aarch64-pc-windows-msvc"));

    public TaskProvider<CargoBuild> getTaskForVariant(String platform, String buildType) {

        TaskProvider<CargoBuild> buildTask = tryFromCache(platform, buildType);
        if (buildTask == null) {
            String taskName = "rust" + platform + buildType;
            buildTask = tasks.register(taskName, CargoBuild.class, cfg -> {
                cfg.getBuildType().set(buildType);
                cfg.getCargoLocator().set(locator);
                cfg.getTriple().set(tripleMap.get(platform));
                cfg.getRootTargetDirectory().set(
                        layout.getBuildDirectory().dir(cfg.getTriple().get() + buildType));
            });

            addToCache(platform, buildType, buildTask);
        }
        return buildTask;
    }
}
