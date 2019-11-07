package edu.wpi.first.nativeutils.configs;

import java.io.File;
import java.util.List;

import org.gradle.api.Named;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.MapProperty;

public interface PrivateExportsConfig extends Named {
  RegularFileProperty getExportsFile();

  MapProperty<String, RegularFileProperty> getPlatformExportsFiles();

  void addFileForPlatforms(List<String> platforms, File file);
}
