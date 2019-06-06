import models.extractFingerprint
import org.junit.Test
import components.*


class SCTest {

    private val logger by logger()

    private val signingReportTask = """
            rm -rf KotlinSample &&
            git clone https://github.com/StringCare/KotlinSample.git &&
            cd KotlinSample &&
            echo "sdk.dir=/Users/efrainespada/Library/Android/sdk" > local.properties &&
            ./gradlew signingReport
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
            val key = report.extractFingerprint()
            assert(key.split(":").size == 20)
        }
    }

}