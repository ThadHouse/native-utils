package edu.wpi.first.toolchain.visualCpp;

public abstract interface CustomVisualCpp extends org.gradle.nativeplatform.toolchain.NativeToolChain {

    public abstract java.io.File getInstallDir();

    public abstract void setInstallDir(java.lang.Object arg0);

    public abstract java.io.File getWindowsSdkDir();

    public abstract void setWindowsSdkDir(java.lang.Object arg0);

    public abstract  void eachPlatform(org.gradle.api.Action<? super org.gradle.nativeplatform.toolchain.VisualCppPlatformToolChain> arg0);
  }
