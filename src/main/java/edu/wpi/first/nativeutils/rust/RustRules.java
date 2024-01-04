package edu.wpi.first.nativeutils.rust;

import java.util.Map;
import java.util.Map.Entry;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.model.Defaults;
import org.gradle.model.Finalize;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.FlavorContainer;
import org.gradle.nativeplatform.PrebuiltLibraries;
import org.gradle.nativeplatform.Repositories;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.Gcc;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.ToolType;
import org.gradle.platform.base.Platform;
import org.gradle.platform.base.PlatformContainer;

public class RustRules extends RuleSource {
    @Finalize
    public void useRegistry(NativeToolChainRegistry toolChainRegistry, PlatformContainer platforms, BuildTypeContainer buildTypes, FlavorContainer flavors, ExtensionContainer extensions) {
        RustProject rustProject = extensions.getByType(RustExtension.class).getRustProject();

        Map<String, Map<String, TaskProvider<CargoBuild>>> buildTasks = rustProject.getBuildTasks();

        for (Entry<String, Map<String, TaskProvider<CargoBuild>>> buildTypeMap : buildTasks.entrySet()) {
            Platform platform = platforms.findByName(buildTypeMap.getKey());
            if (platform == null || !(platform instanceof NativePlatform)) {
                continue;
            }
            // TODO check properly for cross compilation
            NativeToolChain tc = toolChainRegistry.getForPlatform((NativePlatform)platform);
            if (!(tc instanceof Gcc)) {
                continue;
            }
            NativeToolChainInternal toolChain = (NativeToolChainInternal) tc;
            NativePlatformInternal targetPlatform = (NativePlatformInternal) platform;
            PlatformToolProvider toolProvider = toolChain.select(targetPlatform);

            for (Entry<String, TaskProvider<CargoBuild>> tasks : buildTypeMap.getValue().entrySet()) {
                tasks.getValue().configure(task -> {
                    task.getArchiver().set(toolProvider.locateTool(ToolType.STATIC_LIB_ARCHIVER).getTool());
                    task.getLinker().set(toolProvider.locateTool(ToolType.LINKER).getTool());
                });
            }
        }

    }

    @Defaults
    public void addPrebuilts(Repositories repositories, ExtensionContainer extensions, ObjectFactory objects, PlatformContainer platforms, BuildTypeContainer buildTypes, FlavorContainer flavors) {
        PrebuiltLibraries prebuiltLibraries = repositories.create("RustLibraries", PrebuiltLibraries.class);

        for (RustLibrary rustLibrary : extensions.getByType(RustExtension.class).getLibraries()) {
            RustPrebuiltLibrary prebuiltLibrary = new RustPrebuiltLibrary(rustLibrary, objects, platforms, buildTypes, flavors);
            prebuiltLibraries.add(prebuiltLibrary);
        }

    }
}
