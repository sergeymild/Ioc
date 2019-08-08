package debuglog

import org.gradle.api.Project

class DebugLogGradlePlugin : org.gradle.api.Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create("debugLog", DebugLogGradlePluginExtension::class.java)
    }

}