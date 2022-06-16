package edu.wpi.first.toolchain.arm64

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*

import spock.lang.Shared
import spock.lang.TempDir
import spock.lang.Specification
import spock.lang.IgnoreIf

@IgnoreIf({ !Boolean.valueOf(env['SPOCK_RUN_TOOLCHAINS']) })
class Arm64DownloadTest extends Specification {
  @TempDir File testProjectDir
  File buildFile
  @Shared File toolchainDir

  def setup() {
    buildFile = new File(testProjectDir, 'build.gradle')
  }

  def setupSpec() {
    Arm64ToolchainExtension ext = new Arm64ToolchainExtension()
    String arm64Version = ext.toolchainVersion.split("-")[0].toLowerCase();
    toolchainDir = Arm64ToolchainPlugin.toolchainInstallLoc(arm64Version)
    def result = toolchainDir.deleteDir()  // Returns true if all goes well, false otherwise.
    assert result
  }

  def "Toolchain Can Download"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'edu.wpi.first.Toolchain'
}

toolchainsPlugin.withCrossLinuxArm64()
"""
    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('installArm64Toolchain', '--stacktrace')
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':installArm64Toolchain').outcome == SUCCESS
  }
}
