package components

internal fun signingReportTask(): String = "${when (getOs()) {
    Os.WINDOWS -> wrapperWindows
    Os.OSX -> wrapperOsX
}} signingReport"

internal fun gradleWrapper(): String = when (getOs()) {
    Os.WINDOWS -> wrapperWindows
    Os.OSX -> wrapperOsX
}

internal fun pluginBuildTask(): String = "${when (getOs()) {
    Os.WINDOWS -> wrapperWindows
    Os.OSX -> wrapperOsX
}} build -d --exclude-task test"