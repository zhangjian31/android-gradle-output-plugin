package cn.jeryzhang.gradle

import groovy.text.SimpleTemplateEngine
import org.gradle.api.Project

class OutputApkConfigExtension {
    String nameFormat
    String copyTo

    void nameFormat(String format) {
        nameFormat = format
    }

    void copyTo(String path) {
        copyTo = path
    }

    def startCopy(Project project) {
        File outputFile = new File(project.buildDir.path + "/outputs")
        def desFile = new File(copyTo)
        if (desFile.isFile()) {
            desFile.delete()
        }
        if (desFile.exists()) {
            desFile.deleteDir()
        }
        desFile.mkdir()
        outputFile.eachFileRecurse { file ->
            if (file.getName().endsWith(".apk")) {
                project.copy {
                    from(file.path)
                    into(desFile)
                }
            }
        }
    }

    def generateOutputName(Project project, variant) {
        def templateEngine = new SimpleTemplateEngine()
        def map = [
                'appName'    : project.name,
                'projectName': project.rootProject.name,
                'flavorName' : variant.flavorName,
                'buildType'  : variant.buildType.name,
                'versionName': project.android.defaultConfig.versionName,
                'versionCode': project.android.defaultConfig.versionCode,
                'versionCode': project.android.defaultConfig.versionCode
        ]


        def defaultTemplate = !variant.flavorName.equals("") && variant.flavorName != null ?
                '$appName-$flavorName-$buildType-$versionName' : '$appName-$buildType-$versionName'
        def template = nameFormat == null ? defaultTemplate : nameFormat
        def fileName = templateEngine.createTemplate(template).make(map).toString()

        def androidGradlePlugin = SimpleAndroidOutputPlugin.getAndroidPluginVersion(project)

        if (androidGradlePlugin != null && androidGradlePlugin.version.startsWith("3.")) {
            variant.outputs.all { output ->
                outputFileName = "${fileName}.apk"
            }
        } else {
            variant.outputs.each { output ->
                output.outputFile = new File(output.outputFile.parent, fileName + ".apk")
            }
        }
    }
}
