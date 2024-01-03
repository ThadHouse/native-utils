package edu.wpi.first.nativeutils.rust;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.Defaults;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.BuildTypeContainer;
import org.gradle.nativeplatform.FlavorContainer;
import org.gradle.nativeplatform.PrebuiltLibraries;
import org.gradle.nativeplatform.Repositories;
import org.gradle.platform.base.PlatformContainer;

public class RustRules extends RuleSource {
    @Defaults
    public void addPrebuilts(Repositories repositories, ExtensionContainer extensions, ObjectFactory objects, PlatformContainer platforms, BuildTypeContainer buildTypes, FlavorContainer flavors) {
        PrebuiltLibraries prebuiltLibraries = repositories.create("RustLibraries", PrebuiltLibraries.class);

        for (RustLibrary rustLibrary : extensions.getByType(RustExtension.class).getLibraries()) {
            RustPrebuiltLibrary prebuiltLibrary = new RustPrebuiltLibrary(rustLibrary, objects, platforms, buildTypes, flavors);
            prebuiltLibraries.add(prebuiltLibrary);
        }

    }
}
