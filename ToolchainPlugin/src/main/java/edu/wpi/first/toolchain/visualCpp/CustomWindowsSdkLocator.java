/*
 * Copyright 2017 the original author or authors.
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

import net.rubygrapefruit.platform.WindowsRegistry;
import org.gradle.internal.logging.text.DiagnosticsVisitor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.LegacyWindowsSdkLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsComponentLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsKitSdkInstall;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsKitWindowsSdkLocator;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdkInstall;
import org.gradle.nativeplatform.toolchain.internal.msvcpp.WindowsSdkLocator;
import org.gradle.platform.base.internal.toolchain.SearchResult;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomWindowsSdkLocator implements WindowsSdkLocator {
    private final WindowsSdkLocator legacyWindowsSdkLocator;
    private final WindowsComponentLocator<WindowsKitSdkInstall> windowsKitWindowsSdkLocator;

    CustomWindowsSdkLocator(WindowsSdkLocator legacyWindowsSdkLocator, WindowsComponentLocator<WindowsKitSdkInstall> windowsKitWindowsSdkLocator) {
        this.legacyWindowsSdkLocator = legacyWindowsSdkLocator;
        this.windowsKitWindowsSdkLocator = windowsKitWindowsSdkLocator;
    }

    public CustomWindowsSdkLocator(OperatingSystem operatingSystem, WindowsRegistry windowsRegistry) {
        this(new LegacyWindowsSdkLocator(operatingSystem, windowsRegistry), new WindowsKitWindowsSdkLocator(windowsRegistry));
    }

    @Override
    public SearchResult<WindowsSdkInstall> locateComponent(@Nullable File candidate) {
        return new SdkSearchResult(legacyWindowsSdkLocator.locateComponent(candidate), windowsKitWindowsSdkLocator.locateComponent(candidate));
    }

    @Override
    public List<WindowsSdkInstall> locateAllComponents() {
        List<WindowsSdkInstall> allSdks = new ArrayList<WindowsSdkInstall>();
        allSdks.addAll(legacyWindowsSdkLocator.locateAllComponents());
        allSdks.addAll(windowsKitWindowsSdkLocator.locateAllComponents());
        return allSdks;
    }

    private static class SdkSearchResult implements SearchResult<WindowsSdkInstall> {
        final SearchResult<WindowsSdkInstall> legacySearchResult;
        final SearchResult<WindowsKitSdkInstall> windowsKitSearchResult;

        SdkSearchResult(SearchResult<WindowsSdkInstall> legacySearchResult, SearchResult<WindowsKitSdkInstall> windowsKitSearchResult) {
            this.legacySearchResult = legacySearchResult;
            this.windowsKitSearchResult = windowsKitSearchResult;
        }

        @Override
        public WindowsSdkInstall getComponent() {
            if (windowsKitSearchResult.isAvailable()) {
                return windowsKitSearchResult.getComponent();
            } else if (legacySearchResult.isAvailable()) {
                return legacySearchResult.getComponent();
            } else {
                return null;
            }
        }

        @Override
        public boolean isAvailable() {
            return windowsKitSearchResult.isAvailable() || legacySearchResult.isAvailable();
        }

        @Override
        public void explain(DiagnosticsVisitor visitor) {
            windowsKitSearchResult.explain(visitor);
        }
    }
}
