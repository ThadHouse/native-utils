package edu.wpi.first.toolchain;

import org.gradle.internal.os.OperatingSystem;

import java.io.File;

public class FrcHome {

    private String year;
    private File frcFolder;

    public FrcHome(String year) {
        this.year = year;

        File baseFolder;
        if (OperatingSystem.current().isWindows()) {
            String publicFolder = System.getenv("PUBLIC");
            if (publicFolder == null) {
                publicFolder = "C:\\Users\\Public";
            }
            baseFolder = new File(publicFolder, "wpilib");
        } else {
            baseFolder = new File(System.getProperty("user.home"), "wpilib");
        }
        this.frcFolder = new File(baseFolder, year);
    }

    public File get() {
        return frcFolder;
    }

    public String year() {
        return year;
    }
}
