package edu.wpi.first.nativeutils.model;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.Defaults;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.BuildType;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.Flavor;
import org.gradle.nativeplatform.FlavorContainer;
import org.gradle.nativeplatform.NativeLibraryBinary;
import org.gradle.nativeplatform.PrebuiltLibraries;
import org.gradle.nativeplatform.PrebuiltLibrary;
import org.gradle.nativeplatform.Repositories;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.platform.base.PlatformContainer;

import edu.wpi.first.nativeutils.NativeUtilsExtension;
import edu.wpi.first.nativeutils.dependencies.NativeDependency;

public class PrebuiltLibraryRules extends RuleSource {
    @Defaults
    public void addPrebuilts(Repositories repositories, ExtensionContainer extensions, PlatformContainer platforms, BuildTypeContainer buildTypes, FlavorContainer flavors, ObjectFactory objects) {
        PrebuiltLibraries prebuiltLibraries = repositories.create("WpiLibraries", PrebuiltLibraries.class);

        NativeUtilsExtension nue = extensions.getByType(NativeUtilsExtension.class);

        for (NativeDependency dep : nue.getNativeDependencyContainer()) {

            PrebuiltLibrary library = new WpiPrebuiltLibrary(dep.getName(), objects);
            prebuiltLibraries.add(library);

            for (NativePlatform platform : platforms.withType(NativePlatform.class)) {
                 for (BuildType buildType : buildTypes) {
                    for (Flavor flavor : flavors) {
                        NativeLibraryBinary binary = new WpiRequiredPrebuiltBinary(dep, buildType, platform, flavor);
                        library.getBinaries().add(binary);
                        NativeLibraryBinary optional = new WpiOptionalPrebuiltBinary(dep, buildType, platform, flavor);
                        library.getBinaries().add(optional);
                    }
                 }
            }

        }


    }
}
