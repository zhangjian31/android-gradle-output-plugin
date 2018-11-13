package com.jeryzhang.gradle.outputapk

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle

class SimpleAndroidOutputPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def androidGradlePlugin = getAndroidPluginVersion(project);
        if (androidGradlePlugin == null) {
            throw new GradleException("非Android工程不可用！")
        }

        project.extensions.add("outputApk", OutputApkConfigExtension)
        project.afterEvaluate {
            project.android.applicationVariants.all {
                project.outputApk.generateOutputName(project, it)
            }
        }
        project.gradle.addBuildListener(new BuildListener() {
            @Override
            void buildStarted(Gradle gradle) {

            }

            @Override
            void settingsEvaluated(Settings settings) {

            }

            @Override
            void projectsLoaded(Gradle gradle) {

            }

            @Override
            void projectsEvaluated(Gradle gradle) {

            }

            @Override
            void buildFinished(BuildResult buildResult) {
                if (project.outputApk.copyTo != null && !project.outputApk.copyTo.equals("")) {
                    project.outputApk.startCopy(project)
                }
            }
        })
    }


    def static getAndroidPluginVersion(Project project) {
        def projectGradle = findClassPathDependencyVersion(project, 'com.android.tools.build', 'gradle')
        if (projectGradle == null) {
            projectGradle = findClassPathDependencyVersion(project.getRootProject(), 'com.android.tools.build', 'gradle')
        }
        return projectGradle
    }

    def static findClassPathDependencyVersion(Project project, group, attributeId) {
        return project.buildscript.configurations.classpath.dependencies.find {
            it.group != null && it.group.equals(group) && it.name.equals(attributeId)
        }
    }

}
