/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.wpi.first.toolchain.visualCpp;

import org.gradle.api.GradleException;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.platform.internal.ArchitectureInternal;
import org.gradle.nativeplatform.platform.internal.DefaultArchitecture;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.platform.internal.OperatingSystemInternal;
import org.gradle.nativeplatform.toolchain.VisualCppPlatformToolChain;
import org.gradle.nativeplatform.toolchain.internal.EmptySystemLibraries;
import org.gradle.nativeplatform.toolchain.internal.ExtendableToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeLanguage;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.SystemLibraries;
import org.gradle.nativeplatform.toolchain.internal.ToolType;
import org.gradle.nativeplatform.toolchain.internal.UnavailablePlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.UnsupportedPlatformToolProvider;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.DefaultVisualCppPlatformToolChain;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.UcrtInstall;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.UcrtLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualCpp;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualCppInstall;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualCppToolChain;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualStudioInstall;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualStudioLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsKitInstall;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdk;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdkInstall;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdkLocator;
import org.gradle.platform.base.internal.toolchain.SearchResult;
import org.gradle.platform.base.internal.toolchain.ToolChainAvailability;
import org.gradle.platform.base.internal.toolchain.ToolSearchResult;
import org.gradle.process.internal.ExecActionFactory;
import org.gradle.util.VersionNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomVisualCppToolChain extends ExtendableToolChain<VisualCppPlatformToolChain>
        implements CustomVisualCpp {

    private final String name;
    private final OperatingSystem operatingSystem;

    protected static final Logger LOGGER = LoggerFactory.getLogger(CustomVisualCppToolChain.class);

    public static final String DEFAULT_NAME = "visualCpp";

    private final ExecActionFactory execActionFactory;
    private final VisualStudioLocator visualStudioLocator;
    private final WindowsSdkLocator windowsSdkLocator;
    private final UcrtLocator ucrtLocator;
    private final Instantiator instantiator;
    private final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory;
    private final WorkerLeaseService workerLeaseService;

    private File installDir;
    private File ucrtDir;
    private File windowsSdkDir;
    private UcrtInstall ucrt;
    private VisualStudioInstall visualStudio;
    private VisualCppInstall visualCpp;
    private WindowsSdkInstall windowsSdk;
    private ToolChainAvailability availability;

    public CustomVisualCppToolChain(String name, BuildOperationExecutor buildOperationExecutor,
            OperatingSystem operatingSystem, FileResolver fileResolver, ExecActionFactory execActionFactory,
            CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory,
            VisualStudioLocator visualStudioLocator, WindowsSdkLocator windowsSdkLocator, UcrtLocator ucrtLocator,
            Instantiator instantiator, WorkerLeaseService workerLeaseService) {
        super(name, buildOperationExecutor, operatingSystem, fileResolver);
        this.name = name;
        this.operatingSystem = operatingSystem;
        this.execActionFactory = execActionFactory;
        this.compilerOutputFileNamingSchemeFactory = compilerOutputFileNamingSchemeFactory;
        this.visualStudioLocator = visualStudioLocator;
        this.windowsSdkLocator = windowsSdkLocator;
        this.ucrtLocator = ucrtLocator;
        this.instantiator = instantiator;
        this.workerLeaseService = workerLeaseService;
    }

    @Override
    protected String getTypeName() {
        return "Visual Studio";
    }

    @Override
    public File getInstallDir() {
        return installDir;
    }

    @Override
    public void setInstallDir(Object installDirPath) {
        this.installDir = resolve(installDirPath);
    }

    @Override
    public File getWindowsSdkDir() {
        return windowsSdkDir;
    }

    @Override
    public void setWindowsSdkDir(Object windowsSdkDirPath) {
        this.windowsSdkDir = resolve(windowsSdkDirPath);
    }

    public File getUcrtDir() {
        return ucrtDir;
    }

    public void setUcrtDir(Object ucrtDirPath) {
        this.ucrtDir = resolve(ucrtDirPath);
    }

    private class WindowsKitBackedSdk implements WindowsSdk {
        private final String platformDirName;
        private final VersionNumber version;
        private final File binDir;
        private final File baseDir;

        WindowsKitBackedSdk(String platformDirName, VersionNumber version, File binDir, File baseDir) {
            this.platformDirName = platformDirName;
            this.version = version;
            this.binDir = binDir;
            this.baseDir = baseDir;
        }

        @Override
        public VersionNumber getImplementationVersion() {
            return version;
        }

        @Override
        public VersionNumber getSdkVersion() {
            return getImplementationVersion();
        }

        @Override
        public List<File> getIncludeDirs() {
            return Arrays.asList(new File(baseDir, "Include/" + getImplementationVersion().toString() + "/um"),
                    new File(baseDir, "Include/" + getImplementationVersion().toString() + "/shared"));
        }

        @Override
        public List<File> getLibDirs() {
            return Collections.singletonList(
                    new File(baseDir, "Lib/" + getImplementationVersion().toString() + "/um/" + platformDirName));
        }

        @Override
        public File getResourceCompiler() {
            return new File(getBinDir(), "rc.exe");
        }

        @Override
        public Map<String, String> getPreprocessorMacros() {
            return Collections.emptyMap();
        }

        @Override
        public List<File> getPath() {
            return Collections.singletonList(getBinDir());
        }

        private File getBinDir() {
            return new File(binDir, platformDirName);
        }
    }

    private class UcrtSystemLibraries implements SystemLibraries {
        private final String platformDirName;
        private final VersionNumber version;
        private final File baseDir;

        UcrtSystemLibraries(String platformDirName, VersionNumber version, File baseDir) {
            this.platformDirName = platformDirName;
            this.version = version;
            this.baseDir = baseDir;
        }

        @Override
        public List<File> getIncludeDirs() {
            return Collections.singletonList(new File(baseDir, "Include/" + version + "/ucrt"));
        }

        @Override
        public List<File> getLibDirs() {
            return Collections.singletonList(new File(baseDir, "Lib/" + version + "/ucrt/" + platformDirName));
        }

        @Override
        public Map<String, String> getPreprocessorMacros() {
            return Collections.emptyMap();
        }
    }

    private static class PlatformWrapper implements NativePlatformInternal {

        @Override
        public void architecture(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void operatingSystem(String arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public String getDisplayName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public String getName() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public ArchitectureInternal getArchitecture() {
            return new DefaultArchitecture("x86");
        }

        @Override
        public OperatingSystemInternal getOperatingSystem() {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    public PlatformToolProvider select(NativePlatformInternal targetPlatform) {
        ToolChainAvailability result = new ToolChainAvailability();
        result.mustBeAvailable(getAvailability());
        if (!result.isAvailable()) {
            return new UnavailablePlatformToolProvider(targetPlatform.getOperatingSystem(), result);
        }

        VisualCpp platformVisualCpp = visualCpp == null ? null : visualCpp.forPlatform(targetPlatform);
        if (platformVisualCpp == null) {
            return new UnsupportedPlatformToolProvider(targetPlatform.getOperatingSystem(),
                    String.format("Don't know how to build for %s.", targetPlatform.getDisplayName()));
        }
        WindowsSdk baseSdk = windowsSdk.forPlatform(new PlatformWrapper());
        WindowsSdk platformSdk;
        SystemLibraries cRuntime;
        if (targetPlatform.getArchitecture().isAmd64()) {
            platformSdk = new WindowsKitBackedSdk("x64", windowsSdk.getVersion(), baseSdk.getPath().get(0),
                    ((WindowsKitInstall) windowsSdk).getBaseDir());
            cRuntime = ucrt == null ? new EmptySystemLibraries()
                    : new UcrtSystemLibraries("x64", ucrt.getVersion(), ucrt.getBaseDir());
        } else if (targetPlatform.getArchitecture().isArm()) {
            platformSdk = new WindowsKitBackedSdk("arm", windowsSdk.getVersion(), baseSdk.getPath().get(0),
                    ((WindowsKitInstall) windowsSdk).getBaseDir());
            cRuntime = ucrt == null ? new EmptySystemLibraries()
                    : new UcrtSystemLibraries("arm", ucrt.getVersion(), ucrt.getBaseDir());
        } else if (targetPlatform.getArchitecture().isI386()) {
            platformSdk = new WindowsKitBackedSdk("x86", windowsSdk.getVersion(), baseSdk.getPath().get(0),
                    ((WindowsKitInstall) windowsSdk).getBaseDir());
            cRuntime = ucrt == null ? new EmptySystemLibraries()
                    : new UcrtSystemLibraries("x86", ucrt.getVersion(), ucrt.getBaseDir());
        } else {
            platformSdk = new WindowsKitBackedSdk("arm64", windowsSdk.getVersion(), baseSdk.getPath().get(0),
                    ((WindowsKitInstall) windowsSdk).getBaseDir());
            cRuntime = ucrt == null ? new EmptySystemLibraries()
                    : new UcrtSystemLibraries("arm64", ucrt.getVersion(), ucrt.getBaseDir());
        }

        DefaultVisualCppPlatformToolChain configurableToolChain = instantiator
                .newInstance(DefaultVisualCppPlatformToolChain.class, targetPlatform, instantiator);
        configureActions.execute(configurableToolChain);

        try {
            Class<?> cls = VisualCppToolChain.class.getClassLoader()
                    .loadClass("org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualCppPlatformToolProvider");
            Constructor<?> constructor = cls.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            Field field = DefaultVisualCppPlatformToolChain.class.getDeclaredField("tools");
            field.setAccessible(true);
            Object tools = field.get(configurableToolChain);
            return (PlatformToolProvider)constructor.newInstance(buildOperationExecutor, targetPlatform.getOperatingSystem(), tools, visualStudio, platformVisualCpp, platformSdk, cRuntime, execActionFactory, compilerOutputFileNamingSchemeFactory, workerLeaseService);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchFieldException | SecurityException e) {
            throw new GradleException("Failure for reflection", e);
        }
    }

    @Override
    public PlatformToolProvider select(NativeLanguage sourceLanguage, NativePlatformInternal targetMachine) {
        switch (sourceLanguage) {
            case CPP:
                PlatformToolProvider toolProvider = select(targetMachine);
                if (!toolProvider.isAvailable()) {
                    return toolProvider;
                }
                ToolSearchResult cppCompiler = toolProvider.locateTool(ToolType.CPP_COMPILER);
                if (!cppCompiler.isAvailable()) {
                    return new UnavailablePlatformToolProvider(targetMachine.getOperatingSystem(), cppCompiler);
                }
                return toolProvider;
            case ANY:
                return select(targetMachine);
            default:
                return new UnsupportedPlatformToolProvider(targetMachine.getOperatingSystem(), String.format("Don't know how to compile language %s.", sourceLanguage));
        }
    }

    private ToolChainAvailability getAvailability() {
        if (availability == null) {
            availability = new ToolChainAvailability();
            checkAvailable(availability);
        }

        return availability;
    }

    private void checkAvailable(ToolChainAvailability availability) {
        if (!operatingSystem.isWindows()) {
            availability.unavailable("Visual Studio is not available on this operating system.");
            return;
        }

        // TODO - this selection should happen per target platform

        SearchResult<VisualStudioInstall> visualStudioSearchResult = visualStudioLocator.locateComponent(installDir);
        availability.mustBeAvailable(visualStudioSearchResult);
        if (visualStudioSearchResult.isAvailable()) {
            visualStudio = visualStudioSearchResult.getComponent();
            visualCpp = visualStudioSearchResult.getComponent().getVisualCpp();
        }

        SearchResult<WindowsSdkInstall> windowsSdkSearchResult = windowsSdkLocator.locateComponent(windowsSdkDir);
        availability.mustBeAvailable(windowsSdkSearchResult);
        if (windowsSdkSearchResult.isAvailable()) {
            windowsSdk = windowsSdkSearchResult.getComponent();
        }

        // Universal CRT is required only for VS2015
        if (isVisualCpp2015()) {
            SearchResult<UcrtInstall> ucrtSearchResult = ucrtLocator.locateComponent(ucrtDir);
            availability.mustBeAvailable(ucrtSearchResult);
            if (ucrtSearchResult.isAvailable()) {
                ucrt = ucrtSearchResult.getComponent();
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return "Tool chain '" + getName() + "' (" + getTypeName() + ")";
    }

    public boolean isVisualCpp2015() {
        return visualCpp != null && visualCpp.getVersion().getMajor() >= 14;
    }
}
