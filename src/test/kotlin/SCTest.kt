import components.*
import org.junit.Before
import org.junit.Test
import utils.modifyForTest
import java.io.File

class SCTest {

    // private val logger by logger()

    @Before
    fun setup() {
        librarySetupTask.runCommand()
    }

    @Test
    fun `01 - (PLUGIN) terminal verification`() {
        "echo $extensionName".runCommand { command, result ->
            assert(command.contains(result.normalize()))
        }
    }

    @Test
    fun `02 - (PLUGIN) gradlew signingReport`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            assert(report.contains("SHA1") && report.contains("BUILD SUCCESSFUL"))
        }
    }

    @Test
    fun `03 - (PLUGIN) fingerprint extraction`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            assert(report.extractFingerprint().split(":").size == 20)
        }
    }

    @Test
    fun `04 - (PLUGIN) locate string files for default configuration`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(locateFiles("$temp${File.separator}$testProjectName", defaultConfig()).isNotEmpty())
        }
        StringCare.resetFolder()
    }

    @Test
    fun `05 - (PLUGIN) backup string files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(backupFiles("$temp${File.separator}$testProjectName", defaultConfig()).isNotEmpty())
        }
        StringCare.resetFolder()
    }

    @Test
    fun `06 - (PLUGIN) restore string files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(
                restoreFiles("$temp${File.separator}$testProjectName", defaultMainModule).isEmpty()
            )
            assert(
                backupFiles("$temp${File.separator}$testProjectName", defaultConfig().apply {
                    stringFiles.add("strings_extra.xml")
                    srcFolders.add("src${File.separator}other_source")
                }).isNotEmpty()
            )
            assert(
                restoreFiles("$temp${File.separator}$testProjectName", defaultMainModule).isNotEmpty()
            )
        }
    }

    @Test
    fun `07 - (PLUGIN) xml parsing`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            val files = locateFiles("$temp${File.separator}$testProjectName", defaultConfig())
            files.forEach {
                assert(parseXML(it.file).isNotEmpty())
            }
        }
    }

    @Test
    fun `08 - (PLUGIN) obfuscate string values`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            val key = report.extractFingerprint()
            assert(key.isNotEmpty())
            val files = locateFiles("$temp${File.separator}$testProjectName", defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file.file)
                entities.forEach { entity ->
                    val obfuscated = obfuscate(
                        "$temp${File.separator}$mainModuleTest",
                        key,
                        entity
                    )
                    assert(obfuscated.value != entity.value)
                }
            }
        }
    }

    @Test
    fun `09 - (PLUGIN) obfuscate and reveal string values`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            val key = report.extractFingerprint()
            assert(key.isNotEmpty())
            val files = locateFiles("$temp${File.separator}$testProjectName", defaultConfig().apply {
                stringFiles.add("strings_extra.xml")
                srcFolders.add("src${File.separator}other_source")
            })
            files.forEach { file ->
                val entities = parseXML(file.file)
                entities.forEach { entity ->
                    val obfuscated = obfuscate(
                        "$temp${File.separator}$mainModuleTest",
                        key,
                        entity
                    )
                    assert(obfuscated.value != entity.value)

                    val original = reveal(
                        "$temp${File.separator}$mainModuleTest",
                        key,
                        obfuscated
                    )

                    assert(
                        original.value == when (entity.androidTreatment) {
                            true -> entity.value.androidTreatment()
                            else -> entity.value
                        }
                    )

                }
            }
        }
    }

    @Test
    fun `10 - (PLUGIN) obfuscate xml`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            val files = locateFiles("$temp${File.separator}$testProjectName", defaultConfig())
            files.forEach { file ->
                val entities = parseXML(file.file)
                assert(entities.isNotEmpty())
                modifyXML(file.file, "$temp${File.separator}$mainModuleTest", report.extractFingerprint(), true)
            }
            val filesObfuscated = locateFiles("$temp${File.separator}$testProjectName", defaultConfig())
            filesObfuscated.forEach { file ->
                val entities = parseXML(file.file)
                assert(entities.isEmpty())
            }
        }
    }

    @Test
    fun `11 - (ANDROID COMPILATION) obfuscate xml and build (not real workflow)`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            val files = locateFiles("$temp${File.separator}$testProjectName", defaultConfig().apply {
                stringFiles.add("strings_extra.xml")
                srcFolders.add("src${File.separator}other_source")
            })
            files.forEach { file ->
                val entities = parseXML(file.file)
                assert(entities.isNotEmpty())
                modifyXML(
                    file.file,
                    "$temp${File.separator}$mainModuleTest",
                    report.extractFingerprint(),
                    true
                )
            }
            val filesObfuscated = locateFiles(
                "$temp${File.separator}$testProjectName",
                defaultConfig().apply {
                    stringFiles.add("strings_extra.xml")
                    srcFolders.add("src${File.separator}other_source")
                }
            )
            filesObfuscated.forEach { file ->
                val entities = parseXML(file.file)
                assert(entities.isEmpty())
            }
        }
    }

    @Test
    fun `12 - (PLUGIN COMPILATION) plugin with no test`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
        }
    }

    @Test
    fun `13 - (ANDROID COMPILATION) plugin running on Android`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
        }
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            modifyForTest(temp, testProjectName)
            buildTask("$temp${File.separator}$testProjectName").runCommand { _, androidReport ->
                assert(androidReport.contains("BUILD SUCCESSFUL"))
            }
        }
    }

}