package components

import java.io.File

internal const val testProjectName = "KotlinSample"
internal const val defaultMainModule = "app"
internal val mainModuleTest = "$testProjectName${File.separator}$defaultMainModule"

internal const val resourceBackup = "resbackup"
internal const val extensionName = "stringcare"
internal const val winLib = "libsignKey.dll"
internal const val osxLib = "libsignKey.dylib"
internal const val wrapperOsX = "./gradlew"
internal const val wrapperWindows = "gradlew.bat"
internal const val copyCommandOsX = "cp"
internal const val copyCommandWindows = "copy"
internal const val emptyChar = ""

internal const val backupStringRes: String = "backupStringResources"
internal const val obfuscateStringRes: String = "obfuscateStringResources"

internal const val test: String = "Test"
internal const val pre: String = "pre"
internal const val build: String = "Build"
internal const val merge: String = "merge"
internal const val resources: String = "Resources"
