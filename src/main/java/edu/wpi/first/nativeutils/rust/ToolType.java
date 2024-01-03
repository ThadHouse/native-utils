package edu.wpi.first.nativeutils.rust;

public enum ToolType {
    RUSTUP("Rust Installer", "rustup"),
    RUSTC("Rust Compiler", "rustc"),
    CARGO("Rust Build System", "cargo");

    private final String toolName;
    private final String exeName;

    ToolType(String toolName, String exeName) {
        this.toolName = toolName;
        this.exeName = exeName;
    }

    public String getToolName() {
        return toolName;
    }

    public String getExeName() {
        return exeName;
    }
}
