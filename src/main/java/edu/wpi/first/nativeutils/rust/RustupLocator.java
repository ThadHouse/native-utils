package edu.wpi.first.nativeutils.rust;

import java.io.File;
import java.util.Optional;

public class RustupLocator {

    private final ToolSearchPath searchPath;

    public RustupLocator(ToolSearchPath searchPath) {
        this.searchPath = searchPath;
    }

    public Optional<File> findExecutable() {
        return searchPath.locate(ToolType.RUSTUP);
    }

}
