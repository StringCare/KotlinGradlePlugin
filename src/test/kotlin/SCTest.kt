import components.*
import models.extractFingerprint
import org.junit.Test


class SCTest {

    private val logger by logger()

    private val prepareTask = """
            rm -rf KotlinSample &&
            git clone https://github.com/StringCare/KotlinSample.git &&
            cd KotlinSample
        """.trimIndent()

    // gradlew task needs export ANDROID_SDK_ROOT=/Users/efrainespada/Library/Android/sdk
    // echo "sdk.dir=${System.getenv("ANDROID_SDK_ROOT")}" > local.properties &&
    private val signingReportTask = """
            $prepareTask &&
            ${signingReportTask()}
        """.trimIndent()

    @Test
    fun `terminal verification`() {
        "echo $extensionName".runCommand { command, result ->
            assert(command.contains(result.removeNewLines()))
        }
    }

    @Test
    fun `gradlew signingReport`() {
        signingReportTask.runCommand { _, report ->
            assert(report.contains("SHA1") && report.contains("BUILD SUCCESSFUL"))
        }
    }

    @Test
    fun `fingerprint extraction`() {
        signingReportTask.runCommand { _, report ->
            assert(report.extractFingerprint().split(":").size == 20)
        }
    }

    @Test
    fun `locate string files for default configuration`() {
        prepareTask.runCommand { _, _ ->
            assert(locateFiles("KotlinSample", defaultConfig()).isNotEmpty())
        }

    }

}