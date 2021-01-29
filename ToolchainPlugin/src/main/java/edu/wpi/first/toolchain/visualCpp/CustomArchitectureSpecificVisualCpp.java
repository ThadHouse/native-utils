package edu.wpi.first.toolchain.visualCpp;

import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualCpp;
import org.gradle.util.VersionNumber;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomArchitectureSpecificVisualCpp implements VisualCpp {
    private static final String COMPILER_FILENAME = "cl.exe";
    private static final String LINKER_FILENAME = "link.exe";
    private static final String ARCHIVER_FILENAME = "lib.exe";

    private final VersionNumber version;
    private final List<File> paths;
    private final File binDir;
    private final File libDir;
    private final File includeDir;
    private final String assemblerFilename;
    private final Map<String, String> definitions;
    private final File compilerPath;

    CustomArchitectureSpecificVisualCpp(VersionNumber version, List<File> paths, File binDir, File libDir, File compilerPath, File includeDir, String assemblerFilename, Map<String, String> definitions) {
        this.version = version;
        this.paths = paths;
        this.binDir = binDir;
        this.libDir = libDir;
        this.includeDir = includeDir;
        this.assemblerFilename = assemblerFilename;
        this.definitions = definitions;
        this.compilerPath = compilerPath;
    }

    @Override
    public VersionNumber getImplementationVersion() {
        return version;
    }

    @Override
    public File getBinDir() {
        return binDir;
    }

    @Override
    public List<File> getPath() {
        return paths;
    }

    @Override
    public File getCompilerExecutable() {
        return new File(binDir, COMPILER_FILENAME);
    }

    @Override
    public File getLinkerExecutable() {
        return new File(binDir, LINKER_FILENAME);
    }

    @Override
    public File getArchiverExecutable() {
        return new File(binDir, ARCHIVER_FILENAME);
    }

    @Override
    public File getAssemblerExecutable() {
        return new File(binDir, assemblerFilename);
    }

    @Override
    public List<File> getLibDirs() {
        return Collections.singletonList(libDir);
    }

    @Override
    public List<File> getIncludeDirs() {
        return Collections.singletonList(includeDir);
    }

    @Override
    public Map<String, String> getPreprocessorMacros() {
        return definitions;
    }

    public boolean isInstalled() {
        return binDir.exists() && compilerPath.exists() && libDir.exists();
    }
}
