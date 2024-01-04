package edu.wpi.first.nativeutils.rust;

import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.PolymorphicDomainObjectContainerInternal;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.ExecOperations;

public class RustExtension {
    private final ObjectFactory objects;
    private final ToolSearchPath toolSearchPath;
    private final RustupTargetChecker rustupTargetChecker;
    private final CargoLocator cargoLocator;
    private final RustProject rustProject;

    private final ExtensiblePolymorphicDomainObjectContainer<RustLibrary> rustLibraries;

    public Class<? extends RustLibrary> getLibraryTypeClass(String name) {
        @SuppressWarnings("unchecked")
        PolymorphicDomainObjectContainerInternal<RustLibrary> internalDependencies = (PolymorphicDomainObjectContainerInternal<RustLibrary>) rustLibraries;
        Set<? extends java.lang.Class<? extends RustLibrary>> dependencyTypeSet = internalDependencies
                .getCreateableTypes();
        for (Class<? extends RustLibrary> dependencyType : dependencyTypeSet) {
            if (dependencyType.getSimpleName().equals(name)) {
                return dependencyType;
            }
        }
        return null;
    }

    private <T extends RustLibrary> void addRustLibraryType(Class<T> cls) {
        rustLibraries.registerFactory(cls, name -> {
            return objects.newInstance(cls, name, rustProject);
        });
    }

    public ExtensiblePolymorphicDomainObjectContainer<RustLibrary> getLibraries() {
        return rustLibraries;
    }

    @Inject
    public RustExtension(ObjectFactory objects, ExecOperations operations, ProjectLayout layout, TaskContainer tasks) {
        this.toolSearchPath = new ToolSearchPath(OperatingSystem.current());
        this.rustupTargetChecker = new RustupTargetChecker(toolSearchPath, operations);
        this.cargoLocator = new CargoLocator(toolSearchPath, operations);
        this.objects = objects;
        this.rustProject = new RustProject(tasks, cargoLocator, layout);

        this.rustLibraries = objects.polymorphicDomainObjectContainer(RustLibrary.class);
        addRustLibraryType(RustSharedLibrary.class);
        addRustLibraryType(RustStaticLibrary.class);
    }

    public ToolSearchPath getToolSearchPath() {
        return toolSearchPath;
    }

    public RustupTargetChecker getRustupTargetChecker() {
        return rustupTargetChecker;
    }

    public CargoLocator getCargoLocator() {
        return cargoLocator;
    }

    public RustProject getRustProject() {
        return rustProject;
    }
}
