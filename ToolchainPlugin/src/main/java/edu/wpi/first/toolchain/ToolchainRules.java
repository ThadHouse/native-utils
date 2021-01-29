package edu.wpi.first.toolchain;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.model.Defaults;
import org.gradle.model.Finalize;
import org.gradle.model.ModelMap;
import org.gradle.model.Mutate;
import org.gradle.model.RuleSource;
import org.gradle.model.Validate;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.nativeplatform.NativeExecutableBinarySpec;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.test.googletest.GoogleTestTestSuiteBinarySpec;
import org.gradle.nativeplatform.test.tasks.RunTestExecutable;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.NativeToolChainRegistry;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;
import org.gradle.nativeplatform.toolchain.internal.clang.ClangToolChain;
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.UcrtLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.VisualStudioLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdkLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.version.CommandLineToolVersionLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.version.SystemPathVersionLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.version.VisualStudioMetaDataProvider;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.version.WindowsRegistryVersionLocator;
import org.gradle.platform.base.BinaryContainer;
import org.gradle.platform.base.BinarySpec;
import org.gradle.platform.base.PlatformContainer;
import org.gradle.process.internal.ExecActionFactory;

import edu.wpi.first.toolchain.configurable.CrossCompilerConfiguration;
import edu.wpi.first.toolchain.visualCpp.CustomVisualCpp;
import edu.wpi.first.toolchain.visualCpp.CustomVisualCppToolChain;
import edu.wpi.first.toolchain.visualCpp.CustomVisualStudioLocator;
import jaci.gradle.log.ETLogger;
import jaci.gradle.log.ETLoggerFactory;

public class ToolchainRules extends RuleSource {

    private static final ETLogger logger = ETLoggerFactory.INSTANCE.create("ToolchainRules");

    @Finalize
    void addClangArm(NativeToolChainRegistryInternal toolChainRegistry) {
        System.out.println("Finaize rule");
        toolChainRegistry.whenObjectAdded(n -> {
            if (n instanceof ClangToolChain) {
                AbstractGccCompatibleToolChain gcc = (AbstractGccCompatibleToolChain)n;
                gcc.target("osxaarch64", gccToolChain -> {
                    Action<List<String>> m64args = new Action<List<String>>() {
                        @Override
                        public void execute(List<String> args) {
                            args.add("-arch");
                            args.add("arm64");
                        }
                    };
                    gccToolChain.getCppCompiler().withArguments(m64args);
                    gccToolChain.getcCompiler().withArguments(m64args);
                    gccToolChain.getObjcCompiler().withArguments(m64args);
                    gccToolChain.getObjcppCompiler().withArguments(m64args);
                    gccToolChain.getLinker().withArguments(m64args);
                    gccToolChain.getAssembler().withArguments(m64args);
                    System.out.println(gccToolChain);
                });
            }
        });
    }

    private void addVsToolchain(NativeToolChainRegistryInternal toolChainRegistry, ServiceRegistry serviceRegistry) {
        final FileResolver fileResolver = serviceRegistry.get(FileResolver.class);
        final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);
        final Instantiator instantiator = serviceRegistry.get(Instantiator.class);
        final OperatingSystem operatingSystem = serviceRegistry.get(OperatingSystem.class);
        final BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor.class);
        final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory = serviceRegistry.get(CompilerOutputFileNamingSchemeFactory.class);
        final CommandLineToolVersionLocator cmdVersionLocator = serviceRegistry.get(CommandLineToolVersionLocator.class);
        final WindowsRegistryVersionLocator  windowsRegistryLocator = serviceRegistry.get(WindowsRegistryVersionLocator.class);
        final SystemPathVersionLocator systemPathLocator = serviceRegistry.get(SystemPathVersionLocator.class);
        final VisualStudioMetaDataProvider versionDeterminer = serviceRegistry.get(VisualStudioMetaDataProvider.class);
        final VisualStudioLocator visualStudioLocator = new CustomVisualStudioLocator(cmdVersionLocator, windowsRegistryLocator, systemPathLocator, versionDeterminer);
        final UcrtLocator ucrtLocator = serviceRegistry.get(UcrtLocator.class);
        final WindowsSdkLocator windowsSdkLocator = serviceRegistry.get(WindowsSdkLocator.class);
        final WorkerLeaseService workerLeaseService = serviceRegistry.get(WorkerLeaseService.class);

        toolChainRegistry.registerFactory(CustomVisualCpp.class, new NamedDomainObjectFactory<CustomVisualCpp>() {
            @Override
            public CustomVisualCpp create(String name) {
                return instantiator.newInstance(CustomVisualCppToolChain.class, name, buildOperationExecutor, operatingSystem, fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory, visualStudioLocator, windowsSdkLocator, ucrtLocator, instantiator, workerLeaseService);
            }
        });
        toolChainRegistry.registerDefaultToolChain("CustomVisualCpp", CustomVisualCpp.class);
    }

    @Defaults
    void addDefaultToolchains(NativeToolChainRegistryInternal toolChainRegistry, ServiceRegistry serviceRegistry,
            ExtensionContainer extContainer) {
        final FileResolver fileResolver = serviceRegistry.get(FileResolver.class);
        final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);
        final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory = serviceRegistry
                .get(CompilerOutputFileNamingSchemeFactory.class);
        final Instantiator instantiator = serviceRegistry.get(Instantiator.class);
        final BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor.class);
        final CompilerMetaDataProviderFactory metaDataProviderFactory = serviceRegistry
                .get(CompilerMetaDataProviderFactory.class);
        final WorkerLeaseService workerLeaseService = serviceRegistry.get(WorkerLeaseService.class);
        final SystemLibraryDiscovery standardLibraryDiscovery = serviceRegistry.get(SystemLibraryDiscovery.class);

        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        ext.getToolchainDescriptors().all(desc -> {
            logger.info("Descriptor Register: " + desc.getName());
            ToolchainOptions options = new ToolchainOptions(instantiator, buildOperationExecutor,
                    OperatingSystem.current(), fileResolver, execActionFactory, compilerOutputFileNamingSchemeFactory,
                    metaDataProviderFactory, workerLeaseService, standardLibraryDiscovery);
            options.descriptor = desc;

            desc.getRegistrar().register(options, toolChainRegistry, instantiator);
        });

        addVsToolchain(toolChainRegistry, serviceRegistry);
    }

    @Mutate
    void addBuildTypes(BuildTypeContainer buildTypes, final ExtensionContainer extContainer) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        if (ext.registerBuildTypes) {
            buildTypes.maybeCreate("release");
            buildTypes.maybeCreate("debug");
        }
    }

    @Validate
    void disableCrossTests(BinaryContainer binaries, ExtensionContainer extContainer) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        for (GoogleTestTestSuiteBinarySpec binary : binaries.withType(GoogleTestTestSuiteBinarySpec.class)) {
            if (ext.getCrossCompilers().findByName(binary.getTargetPlatform().getName()) != null) {
                for (RunTestExecutable runExe : binary.getTasks().withType(RunTestExecutable.class)) {
                    runExe.onlyIf(t -> {
                        return false;
                    });
                }
            }
        }
    }
    @Mutate
    void addDefaultPlatforms(final ExtensionContainer extContainer, final PlatformContainer platforms) {
        final ToolchainExtension ext = extContainer.getByType(ToolchainExtension.class);

        if (ext.registerPlatforms) {
            NativePlatform desktop = platforms.maybeCreate(NativePlatforms.desktop, NativePlatform.class);
            desktop.architecture(NativePlatforms.desktopArch().replaceAll("-", "_"));

            NativePlatforms.PlatformArchPair[] extraPlatforms = NativePlatforms.desktopExtraPlatforms();

            for (NativePlatforms.PlatformArchPair platform : extraPlatforms) {
                NativePlatform toCreate = platforms.maybeCreate(platform.platformName, NativePlatform.class);
                toCreate.architecture(platform.arch);
            }

            for (CrossCompilerConfiguration config : ext.getCrossCompilers()) {
                NativePlatform configedPlatform = platforms.maybeCreate(config.getName(), NativePlatform.class);
                configedPlatform.architecture(config.getArchitecture());
                configedPlatform.operatingSystem(config.getOperatingSystem());
            }
        }
    }

    @Validate
    void checkEnabledToolchains(final BinaryContainer binaries, final NativeToolChainRegistry toolChains) {
        // Map of platform to toolchains
        Map<String, GccToolChain> gccToolChains = new HashMap<>();
        for (NativeToolChain toolChain : toolChains) {
            if (toolChain instanceof GccToolChain) {
                GccToolChain gccToolChain = (GccToolChain) toolChain;
                for (String name : gccToolChain.getDescriptor().getToolchainPlatforms()) {
                    gccToolChains.put(name, gccToolChain);
                }

            }
        }

        for (BinarySpec oBinary : binaries) {
            if (!(oBinary instanceof NativeBinarySpec)) {
                continue;
            }
            NativeBinarySpec binary = (NativeBinarySpec) oBinary;
            GccToolChain chain = gccToolChains.getOrDefault(binary.getTargetPlatform().getName(), null);
            // Can't use getToolChain, as that is invalid for unknown platforms
            if (chain != null) {
                chain.setUsed(true);
            }
        }
    }

    @Mutate
    void createNativeStripTasks(final ModelMap<Task> tasks, final ExtensionContainer extContainer,
            final BinaryContainer binaries) {

        final Project project = extContainer.getByType(ToolchainPlugin.ProjectWrapper.class).getProject();
        final ToolchainExtension tcExt = extContainer.getByType(ToolchainExtension.class);

        for (BinarySpec oBinary : binaries) {
            if (!(oBinary instanceof NativeBinarySpec)) {
                continue;
            }
            NativeBinarySpec binary = (NativeBinarySpec) oBinary;

            GccToolChain gccTmp = null;
            if (binary.getToolChain() instanceof GccToolChain) {
                gccTmp = (GccToolChain) binary.getToolChain();
            } else {
                continue;
            }

            GccToolChain gcc = gccTmp;
            Task rawLinkTask = null;
            if (binary instanceof SharedLibraryBinarySpec) {
                rawLinkTask = ((SharedLibraryBinarySpec) binary).getTasks().getLink();
            } else if (binary instanceof NativeExecutableBinarySpec) {
                rawLinkTask = ((NativeExecutableBinarySpec) binary).getTasks().getLink();
            }
            if (!(rawLinkTask instanceof AbstractLinkTask)) {
                continue;
            }
            AbstractLinkTask linkTask = (AbstractLinkTask) rawLinkTask;

            Map<AbstractLinkTask, OrderedStripTask> linkTaskMap = tcExt.getLinkTaskMap();
            OrderedStripTask stripTask = linkTaskMap.get(linkTask);
            if (stripTask == null) {
                stripTask = new OrderedStripTask(tcExt, binary, linkTask, gcc, project);
                linkTaskMap.put(linkTask, stripTask);
                linkTask.doLast(stripTask);
            }

            stripTask.setPerformDebugStrip(true);
        }
    }
}
