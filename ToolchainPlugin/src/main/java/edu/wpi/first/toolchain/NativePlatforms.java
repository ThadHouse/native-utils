package edu.wpi.first.toolchain;

import org.gradle.internal.os.OperatingSystem;

public class NativePlatforms {
    public static final String desktop = desktopOS() + desktopArch();
    public static final String roborio = "linuxathena";
    public static final String raspbian = "linuxraspbian";
    public static final String aarch64bionic = "linuxaarch64bionic";
    public static final String aarch64xenial = "linuxaarch64xenial";

    public static String desktopArch() {
        String arch = System.getProperty("os.arch");
        return (arch.equals("amd64") || arch.equals("x86_64")) ? "x86-64" : "x86";
    }

    public static class PlatformArchPair {
        public String platformName;
        public String arch;

        public PlatformArchPair(String platformName, String arch) {
            this.platformName = platformName;
            this.arch = arch;
        }
    }

    public static PlatformArchPair[] desktopExtraPlatforms() {
        if (OperatingSystem.current().isWindows()) {
            String currentArch = desktopArch();
            if (currentArch.equals("x86-64")) {
                return new PlatformArchPair[] {new PlatformArchPair("windowsaarch64", "arm64"), new PlatformArchPair("windowsx86", "x86")};
            } else if (currentArch.equals("x86")) {
                return new PlatformArchPair[] {new PlatformArchPair("windowsaarch64", "arm64"), new PlatformArchPair("windowsx86-64", "x86-64")};
            } else {
                return new PlatformArchPair[] {new PlatformArchPair("windowsx86-64", "x86-64"), new PlatformArchPair("windowsx86", "x86")};
            }
        } else if (OperatingSystem.current().isMacOsX()) {
            String currentArch = desktopArch();
            if (currentArch.equals("x86-64")) {
                return new PlatformArchPair[] {new PlatformArchPair("osxaarch64", "arm64")};
            } else {
                return new PlatformArchPair[] {new PlatformArchPair("osxx86-64", "x86-64")};
            }
        } else {
            return new PlatformArchPair[0];
        }
    }

    public static String desktopOS() {
        return OperatingSystem.current().isWindows() ? "windows" : OperatingSystem.current().isMacOsX() ? "osx" : "linux";
    }
}
