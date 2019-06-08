import components.*
import org.junit.Before
import org.junit.Test
import java.io.File


class SCTest {

    private val logger by logger()

    private val projectName = "KotlinSample"
    private val mainModuleTest = "app"

    private val librarySetupTask = """
            cp src/main/kotlin/components/jni/$osxLib out/production/classes/$osxLib
            cp src/main/kotlin/components/jni/$winLib out/production/classes/$winLib
        """.trimIndent()

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

    @Before
    fun setup() {
        librarySetupTask.runCommand()
    }

    @Test
    fun `1 - terminal verification`() {
        "echo $extensionName".runCommand { command, result ->
            assert(command.contains(result.removeNewLines()))
        }
    }

    @Test
    fun `2 - gradlew signingReport`() {
        signingReportTask.runCommand { _, report ->
            assert(report.contains("SHA1") && report.contains("BUILD SUCCESSFUL"))
        }
    }

    @Test
    fun `3 - fingerprint extraction`() {
        signingReportTask.runCommand { _, report ->
            assert(report.extractFingerprint().split(":").size == 20)
        }
    }

    @Test
    fun `4 - locate string files for default configuration`() {
        prepareTask.runCommand { _, _ ->
            assert(locateFiles(projectName, defaultConfig()).isNotEmpty())
        }
    }

    @Test
    fun `5 - backup string files`() {
        prepareTask.runCommand { _, _ ->
            assert(backupFiles(projectName, defaultConfig()).isNotEmpty())
        }
    }

    @Test
    fun `6 - restore string files`() {
        prepareTask.runCommand { _, _ ->
            assert(backupFiles(projectName, defaultConfig()).isNotEmpty())
            assert(restoreFiles(projectName).isNotEmpty())
        }
    }

    @Test
    fun `7 - xml parsing`() {
        prepareTask.runCommand { _, _ ->
            val files = backupFiles(projectName, defaultConfig())
            files.forEach {
                assert(parseXML(it, mainModuleTest, true).isNotEmpty())
            }
        }
    }


    @Test
    fun `8 - obfuscate string values`() {
        signingReportTask.runCommand { _, report ->
            val files = backupFiles(projectName, defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file, mainModuleTest, true)
                entities.forEach { entity ->
                    val obfuscated = obfuscate(
                        "$projectName${File.separator}$mainModuleTest",
                        report.extractFingerprint(),
                        entity
                    )
                    assert(obfuscated.value != entity.value)
                }
            }
        }
    }

    @Test
    fun `9 - obfuscate and reveal string values`() {
        signingReportTask.runCommand { _, report ->
            val files = backupFiles(projectName, defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file, mainModuleTest, true)
                entities.forEach { entity ->
                    val obfuscated = obfuscate(
                        "$projectName${File.separator}$mainModuleTest",
                        report.extractFingerprint(),
                        entity
                    )
                    assert(obfuscated.value != entity.value)

                    val original = reveal(
                        "$projectName${File.separator}$mainModuleTest",
                        report.extractFingerprint(),
                        obfuscated
                    )

                    assert(original.value == entity.value)

                }
            }
        }
    }

}