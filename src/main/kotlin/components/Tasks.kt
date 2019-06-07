package components

internal fun signingReportTask(): String = "${when (getOs()) {
    Os.WINDOWS -> wrapperWindows
    Os.OSX -> wrapperOsX
}} signingReport"