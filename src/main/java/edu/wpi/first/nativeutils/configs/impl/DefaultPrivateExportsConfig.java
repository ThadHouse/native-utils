package edu.wpi.first.nativeutils.configs.impl;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;

import edu.wpi.first.nativeutils.configs.PrivateExportsConfig;

public class DefaultPrivateExportsConfig implements PrivateExportsConfig {
  private RegularFileProperty exportsFile;

  private final MapProperty<String, RegularFileProperty> platformExportsFiles;

  private final String name;
  private final ObjectFactory factory;

  @Inject
  public DefaultPrivateExportsConfig(String name, ObjectFactory factory) {
    exportsFile = factory.fileProperty();
    platformExportsFiles = factory.mapProperty(String.class, RegularFileProperty.class);
    this.name = name;
    this.factory = factory;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public RegularFileProperty getExportsFile() {
    return exportsFile;
  }

  @Override
  public MapProperty<String, RegularFileProperty> getPlatformExportsFiles() {
    return platformExportsFiles;
  }

  @Override
  public void addFileForPlatforms(List<String> platforms, File file) {
    for (String plat : platforms) {
      RegularFileProperty fileProp = factory.fileProperty();
      fileProp.set(file);
      platformExportsFiles.put(plat, fileProp);
    }
  }
}
