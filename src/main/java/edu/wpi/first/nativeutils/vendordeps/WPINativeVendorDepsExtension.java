package edu.wpi.first.nativeutils.vendordeps;

import javax.inject.Inject;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.platform.base.VariantComponentSpec;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.dependencies.AllPlatformsCombinedNativeDependency;
import edu.wpi.first.nativeutils.dependencies.WPIVendorMavenDependency;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.CppArtifact;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.JsonDependency;
import edu.wpi.first.nativeutils.vendordeps.WPIVendorDepsExtension.NamedJsonDependency;

public class WPINativeVendorDepsExtension {
    private final WPIVendorDepsExtension vendorDeps;
    private final Project project;
    private NativeUtilsExtension nte;

    @Inject
    public WPINativeVendorDepsExtension(WPIVendorDepsExtension vendorDeps, Project project) {
        this.vendorDeps = vendorDeps;
        this.project = project;
    }

    public void initializeNativeDependencies() {
        nte = project.getExtensions().getByType(NativeUtilsExtension.class);
        var dependencyContainer = nte.getNativeDependencyContainer();
        dependencyContainer.registerFactory(WPIVendorMavenDependency.class, name -> {
            return project.getObjects().newInstance(WPIVendorMavenDependency.class, name, project);
        });

        vendorDeps.getDependencySet().all(d -> {
            JsonDependency dep = d.getDependency();
            // Individual dependencies
            if (dep.cppDependencies.length <= 0) {
                return;
            }

            String depName = dep.uuid + "_" + dep.name;

            AllPlatformsCombinedNativeDependency combinedDep = dependencyContainer.create(depName,
                    AllPlatformsCombinedNativeDependency.class);

            for (CppArtifact cpp : dep.cppDependencies) {
                String name = depName + "_" + cpp.libName;
                combinedDep.getDependencies().add(name);
                WPIVendorMavenDependency vendorDep = dependencyContainer.create(name, WPIVendorMavenDependency.class);
                vendorDep.setArtifact(cpp);
            }
        });
    }

    public void cpp(Object scope, String... ignore) {
        if (scope instanceof VariantComponentSpec) {
            ((VariantComponentSpec) scope).getBinaries().withType(NativeBinarySpec.class).all(bin -> {
                cppVendorLibForBin(bin, ignore);
            });
        } else if (scope instanceof NativeBinarySpec) {
            cppVendorLibForBin((NativeBinarySpec) scope, ignore);
        } else {
            throw new GradleException(
                    "Unknown type for useVendorLibraries target. You put this declaration in a weird place.");
        }
    }

    private void cppVendorLibForBin(NativeBinarySpec bin, String[] ignore) {
        for (NamedJsonDependency namedDep : vendorDeps.getDependencySet()) {
            JsonDependency dep = namedDep.getDependency();
            if (vendorDeps.isIgnored(ignore, dep)) {
                continue;
            }
            nte.useRequiredLibrary(bin, dep.uuid + "_" + dep.name);
        }
    }
}
