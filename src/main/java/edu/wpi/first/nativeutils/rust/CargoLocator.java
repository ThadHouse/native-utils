package edu.wpi.first.nativeutils.rust;

import java.io.File;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.io.StreamByteBuffer;
import org.gradle.process.ExecOperations;

public class CargoLocator {
    private final ToolSearchPath toolSearchPath;
    private final ExecOperations operations;
    private final Logger logger;

    private File cargo;

    public CargoLocator(ToolSearchPath toolSearchPath, ExecOperations operations) {
        this.toolSearchPath = toolSearchPath;
        this.operations = operations;
        logger = Logging.getLogger(CargoLocator.class);
    }

    public File findCargo() {
        if (cargo == null) {
            checkCargo();
        }
        return cargo;
    }

    private void checkCargo() {
        File cargo = toolSearchPath.locate(ToolType.CARGO).orElseThrow(() -> new RuntimeException("Failed to locate cargo"));
        StreamByteBuffer buffer = new StreamByteBuffer();
        operations.exec(spec -> {
            spec.executable(cargo.getAbsolutePath());
            spec.setWorkingDir(cargo.getParentFile());
            spec.args("--version");
            spec.setStandardOutput(buffer.getOutputStream());
        });
        logger.info(buffer.readAsString());
        this.cargo = cargo;
    }
}
