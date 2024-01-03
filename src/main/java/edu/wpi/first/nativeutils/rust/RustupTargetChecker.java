package edu.wpi.first.nativeutils.rust;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.internal.io.StreamByteBuffer;
import org.gradle.process.ExecOperations;

public class RustupTargetChecker {
    private final ToolSearchPath toolSearchPath;
    private final ExecOperations operations;
    private final Logger logger;

    private List<String> installedTargets = null;

    public RustupTargetChecker(ToolSearchPath toolSearchPath, ExecOperations operations) {
        this.toolSearchPath = toolSearchPath;
        this.operations = operations;
        logger = Logging.getLogger(RustupTargetChecker.class);
    }

    public void verifyTarget(String target) {
        if (installedTargets == null) {
            loadInstalledTargets();
        }

        if (!installedTargets.contains(target)) {
            throw new RuntimeException("Target " + target + " not installed. Use rustup to install");
        }
    }

    private void loadInstalledTargets() {
        File rustup = toolSearchPath.locate(ToolType.RUSTUP).orElseThrow(() -> new RuntimeException("Failed to locate rustup"));
        StreamByteBuffer buffer = new StreamByteBuffer();
        operations.exec(spec -> {
            spec.executable(rustup.getAbsolutePath());
            spec.setWorkingDir(rustup.getParentFile());
            spec.args("target", "list");
            spec.setStandardOutput(buffer.getOutputStream());
        });
        String result = buffer.readAsString();
        String[] split = result.split("\\R");
        List<String> installed = new ArrayList<>();
        for (String target : split) {
            if (target.contains("(installed)")) {
                String foundTarget = target.split(" ")[0];
                installed.add(foundTarget);
                logger.info("Found rust target " + foundTarget);
            }
        }
        installedTargets = installed;
    }
}
