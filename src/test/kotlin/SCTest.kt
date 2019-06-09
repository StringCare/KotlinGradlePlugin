import components.*
import models.StringEntity
import org.junit.Before
import org.junit.Test
import java.io.File


class SCTest {

    private val logger by logger()

    private val projectName = "KotlinSample"
    private val mainModule = "app"
    private val mainModuleTest = "$projectName${File.separator}$mainModule"

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
            val files = locateFiles(projectName, defaultConfig())
            files.forEach {
                assert(parseXML(it.file, mainModuleTest, true).isNotEmpty())
            }
        }
    }


    @Test
    fun `8 - obfuscate string values`() {
        signingReportTask.runCommand { _, report ->
            val files = locateFiles(projectName, defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file.file, mainModuleTest, true)
                entities.forEach { entity ->
                    val obfuscated = obfuscate(
                        mainModuleTest,
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
            val files = locateFiles(projectName, defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file.file, mainModuleTest, true)
                entities.forEach { entity ->
                    val obfuscated = obfuscate(
                        mainModuleTest,
                        report.extractFingerprint(),
                        entity
                    )
                    assert(obfuscated.value != entity.value)

                    val original = reveal(
                        mainModuleTest,
                        report.extractFingerprint(),
                        obfuscated
                    )

                    assert(original.value == entity.value)

                }
            }
        }
    }

    @Test
    fun `10 - obfuscate xml`() {
        signingReportTask.runCommand { _, report ->
            val files = locateFiles(projectName, defaultConfig())
            files.forEach { file ->
                modifyXML(file.file, mainModuleTest, report.extractFingerprint(), true)
            }
            val filesObfuscated = locateFiles(projectName, defaultConfig())
            filesObfuscated.forEach { file ->
                val entities = parseXML(file.file, mainModuleTest, true)
                assert(entities.isEmpty())
            }
        }
    }

}