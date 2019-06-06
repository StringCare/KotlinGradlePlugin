package components

import models.Os
import models.getOs

internal fun signingReportTask(): String = "${when (getOs()) {
    Os.WINDOWS -> wrapperOsX
    Os.OSX -> wrapperWindows
}} signingReport"