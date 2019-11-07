package edu.wpi.first.nativeutils.tasks;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class PrivateExportsGenerationTask extends DefaultTask {
  private final ListProperty<RegularFileProperty> symbolsToExportFiles;

  private final RegularFileProperty exportsFile;

  private final ListProperty<String> exportsList;

  private final Property<String> libraryName;

  @InputFiles
  public ListProperty<RegularFileProperty> getSymbolsToExportFiles() {
    return symbolsToExportFiles;
  }

  @OutputFile
  public RegularFileProperty getExportsFile() {
    return exportsFile;
  }

  @Internal
  public ListProperty<String> getExportsList() {
    return exportsList;
  }

  @Input
  public Property<String> getLibraryName() {
    return libraryName;
  }

  private boolean isWindows = false;
  private boolean isMac = false;

  @Internal
  public void setIsWindows(boolean set) {
    isWindows = set;
  }
  @Internal
  public void setIsMac(boolean set) {
    isMac = set;
  }

  @Inject
  public PrivateExportsGenerationTask(ObjectFactory factory) {
    symbolsToExportFiles = factory.listProperty(RegularFileProperty.class);
    exportsFile = factory.fileProperty();
    exportsList = factory.listProperty(String.class);
    libraryName = factory.property(String.class);

    this.getInputs().files(symbolsToExportFiles);
    this.getOutputs().file(exportsFile);
  }

  private void executeWindows() throws IOException {
    for (RegularFileProperty file : getSymbolsToExportFiles().get()) {
      exportsList.addAll(Files.readAllLines(file.get().getAsFile().toPath()));
    }
    exportsList.finalizeValue();


    File toWrite = exportsFile.get().getAsFile();
    toWrite.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(toWrite.toPath())) {
      writer.write("LIBRARY ");
      writer.write(libraryName.get());
      writer.newLine();
      writer.write("EXPORTS");
      writer.newLine();
      for (String export : exportsList.get()) {
        writer.write("  ");
        writer.write(export);
        writer.newLine();
      }
      writer.flush();
    }
  }

  private void executeUnix() throws IOException {
    for (RegularFileProperty file : getSymbolsToExportFiles().get()) {
      exportsList.addAll(Files.readAllLines(file.get().getAsFile().toPath()));
    }
    exportsList.finalizeValue();


    File toWrite = exportsFile.get().getAsFile();
    toWrite.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(toWrite.toPath())) {
      writer.write(libraryName.get());
      writer.write(" {");
      writer.newLine();
      writer.write("  global: ");
      for (String export : exportsList.get()) {
        writer.write(export);
        writer.write("; ");
      }
      writer.newLine();
      writer.write("  local: *;");
      writer.newLine();
      writer.write("};");
      writer.newLine();
      writer.flush();
    }
  }

  private void executeMac() throws IOException {
    for (RegularFileProperty file : getSymbolsToExportFiles().get()) {
      exportsList.addAll(Files.readAllLines(file.get().getAsFile().toPath()));
    }
    exportsList.finalizeValue();


    File toWrite = exportsFile.get().getAsFile();
    toWrite.getParentFile().mkdirs();

    try (BufferedWriter writer = Files.newBufferedWriter(toWrite.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
      for (String export : exportsList.get()) {
        writer.write("_");
        writer.write(export);
        writer.newLine();
      }
      writer.flush();
    }
  }

  @TaskAction
  public void execute() throws IOException {
    if (isWindows) {
      executeWindows();
    } else if (isMac) {
      executeMac();
    } else {
      executeUnix();
    }
  }
}
