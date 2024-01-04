package edu.wpi.first.nativeutils.rust;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.UntrackedTask;
import org.gradle.process.ExecOperations;

@UntrackedTask(because = "Goes to External Build")
public abstract class CargoBuild extends DefaultTask {
    private final RegularFileProperty linker;
    private final RegularFileProperty archiver;
    private final MapProperty<String, String> rustFlags;
    private final Property<String> triple;
    private final ListProperty<String> extraCargoArgs;
    private final DirectoryProperty rootTargetDirectory;
    private final ExecOperations exec;
    private final Property<CargoLocator> cargo;
    private final Provider<Directory> targetDirectory;
    private final Property<String> buildType;
    private final FileSystemOperations fileOperations;

    @OutputDirectory
    public abstract DirectoryProperty getDependenciesDirectory();

    @InputFiles
    public abstract ConfigurableFileCollection getDependencyFiles();

    @Internal
    public Property<CargoLocator> getCargoLocator() {
        return cargo;
    }

    @Internal
    public RegularFileProperty getLinker() {
        return linker;
    }

    @Internal
    public RegularFileProperty getArchiver() {
        return archiver;
    }

    @Internal
    public MapProperty<String, String> getRustFlags() {
        return rustFlags;
    }

    @Internal
    public Property<String> getTriple() {
        return triple;
    }

    @Internal
    public ListProperty<String> getExtraCargoArgs() {
        return extraCargoArgs;
    }

    @Internal
    public Property<String> getBuildType() {
        return buildType;
    }

    @OutputDirectory
    public DirectoryProperty getRootTargetDirectory() {
        return rootTargetDirectory;
    }

    @OutputDirectory
    public Provider<Directory> getTargetDirectory() {
        return targetDirectory;
    }

    @Inject
    public CargoBuild(ObjectFactory objects, ExecOperations exec, ProviderFactory providers, FileSystemOperations fileOperations) {
        linker = objects.fileProperty();
        archiver = objects.fileProperty();
        rustFlags = objects.mapProperty(String.class, String.class);
        triple = objects.property(String.class);
        extraCargoArgs = objects.listProperty(String.class);
        rootTargetDirectory = objects.directoryProperty();
        cargo = objects.property(CargoLocator.class);
        buildType = objects.property(String.class);

        Callable<String> cbl = () -> getTriple().get() + "/" + getBuildType().get();
        this.targetDirectory = rootTargetDirectory.dir(providers.provider(cbl));

        this.exec = exec;
        this.fileOperations = fileOperations;
    }

    @TaskAction
    public void run() {
        exec.exec(spec -> {
            spec.workingDir(getProject().getProjectDir());
            spec.executable(cargo.get().findCargo().getAbsolutePath());
            spec.args("build", "--target=" + triple.get(), "--target-dir=" + rootTargetDirectory.get().getAsFile().getAbsolutePath());
            if (getBuildType().get().equalsIgnoreCase("release")) {
                spec.args("--release");
            }
            spec.args(extraCargoArgs.getOrElse(List.of()));

            String upperTriple = triple.get().toUpperCase().replace("-", "_");
            if (getLinker().isPresent()) {
                spec.environment("CARGO_TARGET_" + upperTriple + "_LINKER", getLinker().get().getAsFile().getAbsolutePath());
            }
            if (getArchiver().isPresent()) {
                spec.environment("CARGO_TARGET_" + upperTriple + "_AR", getArchiver().get().getAsFile().getAbsolutePath());
            }
        });
    }
}
