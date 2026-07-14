package org.wpilib.nativeutils

import org.gradle.testkit.runner.GradleRunner
import static org.gradle.testkit.runner.TaskOutcome.*
import spock.lang.TempDir
import spock.lang.Specification


class NativeUtilsPluginInitializationTest extends Specification {
  @TempDir File testProjectDir
  File buildFile

  def setup() {
    buildFile = new File(testProjectDir, 'build.gradle')
  }

  def "Project Initializes Correctly"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'org.wpilib.NativeUtils'
}
"""
    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('tasks', '--stacktrace')
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':tasks').outcome == SUCCESS
  }

  def "On disk dependencies can be configured"() {
    given:
    buildFile << """plugins {
  id 'cpp'
  id 'org.wpilib.NativeUtils'
}

def localDependency = nativeUtils.nativeDependencyContainer.create(
    'localLibrary', org.wpilib.nativeutils.dependencies.WPIOnDiskDependency)
localDependency.headers = layout.projectDirectory.dir('include')
localDependency.buildDependencies.put('linuxx64', ['lib/liblocal.a'])
localDependency.runtimeDependencies.put('linuxx64', ['runtime/liblocal.so'])

tasks.register('verifyOnDiskDependency') {
  doLast {
    assert nativeUtils.getNativeDependencyTypeClass('WPIOnDiskDependency') ==
        org.wpilib.nativeutils.dependencies.WPIOnDiskDependency
    assert localDependency.headers.get().asFile == file('include')
    assert localDependency.buildDependencies.get().linuxx64 == ['lib/liblocal.a']
    assert localDependency.runtimeDependencies.get().linuxx64 == ['runtime/liblocal.so']
  }
}
"""

    when:
    def result = GradleRunner.create()
                             .withProjectDir(testProjectDir)
                             .withArguments('verifyOnDiskDependency', '--stacktrace')
                             .withPluginClasspath()
                             .build()

    then:
    result.task(':verifyOnDiskDependency').outcome == SUCCESS
  }
}
