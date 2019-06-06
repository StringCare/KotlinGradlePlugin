import org.junit.Test
import utils.*


class SCTest {

    private val logger by logger()

    private val signingReportTask = """
            rm -rf KotlinSample &&
            git clone https://github.com/StringCare/KotlinSample.git &&
            cd KotlinSample &&
            echo "sdk.dir=/Users/efrainespada/Library/Android/sdk" > local.properties &&
            ./gradlew build &&
            ./gradlew signingReport
        """.trimIndent()

    @Test
    fun `terminal verification`() {
        "echo $extensionName".run { command, result ->
            assert(command.contains(result.removeNewLines()))
        }
    }


    @Test
    fun `gradlew signingReport`() {
        signingReportTask.run { _, report ->
            assert(report.contains("SHA1") && report.contains("BUILD SUCCESSFUL"))
        }
    }

}