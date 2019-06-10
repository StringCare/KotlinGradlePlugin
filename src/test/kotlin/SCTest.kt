import components.*
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

    private val buildTask = """
        cd $projectName
        ${gradleWrapper()} build
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
    fun `01 - (PLUGIN) terminal verification`() {
        "echo $extensionName".runCommand { command, result ->
            assert(command.contains(result.removeNewLines()))
        }
    }

    @Test
    fun `02 - (PLUGIN) gradlew signingReport`() {
        signingReportTask.runCommand { _, report ->
            assert(report.contains("SHA1") && report.contains("BUILD SUCCESSFUL"))
        }
    }

    @Test
    fun `03 - (PLUGIN) fingerprint extraction`() {
        signingReportTask.runCommand { _, report ->
            assert(report.extractFingerprint().split(":").size == 20)
        }
    }

    @Test
    fun `04 - (PLUGIN) locate string files for default configuration`() {
        prepareTask.runCommand { _, _ ->
            assert(locateFiles(projectName, defaultConfig()).isNotEmpty())
        }
    }

    @Test
    fun `05 - (PLUGIN) backup string files`() {
        prepareTask.runCommand { _, _ ->
            assert(backupFiles(projectName, defaultConfig()).isNotEmpty())
        }
    }

    @Test
    fun `06 - (PLUGIN) restore string files`() {
        prepareTask.runCommand { _, _ ->
            assert(restoreFiles(projectName, mainModule).isEmpty())
            assert(backupFiles(projectName, defaultConfig()).isNotEmpty())
            assert(restoreFiles(projectName, mainModule).isNotEmpty())
        }
    }

    @Test
    fun `07 - (PLUGIN) xml parsing`() {
        prepareTask.runCommand { _, _ ->
            val files = locateFiles(projectName, defaultConfig())
            files.forEach {
                assert(parseXML(it.file, true).isNotEmpty())
            }
        }
    }


    @Test
    fun `08 - (PLUGIN) obfuscate string values`() {
        signingReportTask.runCommand { _, report ->
            val files = locateFiles(projectName, defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file.file, true)
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
    fun `09 - (PLUGIN) obfuscate and reveal string values`() {
        signingReportTask.runCommand { _, report ->
            val files = locateFiles(projectName, defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file.file, true)
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
    fun `10 - (PLUGIN) obfuscate xml`() {
        signingReportTask.runCommand { _, report ->
            val files = locateFiles(projectName, defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file.file, true)
                assert(entities.isNotEmpty())
                modifyXML(file.file, mainModuleTest, report.extractFingerprint(), true)
            }
            val filesObfuscated = locateFiles(projectName, defaultConfig())
            filesObfuscated.forEach { file ->
                val entities = parseXML(file.file, true)
                assert(entities.isEmpty())
            }
        }
    }

    @Test
    fun `11 - (ANDROID COMPILATION) obfuscate xml and build (not real workflow)`() {
        signingReportTask.runCommand { _, report ->
            val files = locateFiles(projectName, defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file.file, true)
                assert(entities.isNotEmpty())
                modifyXML(file.file, mainModuleTest, report.extractFingerprint(), true)
            }
            val filesObfuscated = locateFiles(projectName, defaultConfig())
            filesObfuscated.forEach { file ->
                val entities = parseXML(file.file, true)
                assert(entities.isEmpty())
            }
            buildTask.runCommand { _, androidReport ->
                assert(androidReport.contains("BUILD SUCCESSFUL"))
            }
        }
    }

    @Test
    fun `12 - (PLUGIN COMPILATION) plugin with no test`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
        }
    }

}