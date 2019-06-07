import components.*
import org.junit.Test


class SCTest {

    private val logger by logger()

    private val projectName = "KotlinSample"
    private val prepareTask = """
            rm -rf $projectName &&
            git clone https://github.com/StringCare/$projectName.git &&
            cd $projectName
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
            assert(locateFiles(projectName, defaultConfig()).isNotEmpty())
        }
    }

    @Test
    fun `backup string files`() {
        prepareTask.runCommand { _, _ ->
            assert(backupFiles(projectName, defaultConfig()).isNotEmpty())
        }
    }

    @Test
    fun `restore string files`() {
        prepareTask.runCommand { _, _ ->
            assert(backupFiles(projectName, defaultConfig()).isNotEmpty())
            assert(restoreFiles(projectName).isNotEmpty())
        }
    }

    @Test
    fun `xml parsing`() {
        prepareTask.runCommand { _, _ ->
            val files = backupFiles(projectName, defaultConfig())
            files.forEach {
                assert(parseXML(it, "app", true).isNotEmpty())
            }
        }
    }


    @Test
    fun `obfuscate string files`() {
        prepareTask.runCommand { _, _ ->
            val files = backupFiles(projectName, defaultConfig())
            files.forEach {
                val entities = parseXML(it, "app", true)

            }
        }
    }

}