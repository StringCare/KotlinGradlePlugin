import components.*
import org.junit.Before
import org.junit.Test
import java.io.File

class AssetsTest {

    // private val logger by logger()

    private val configuration = defaultConfig().apply {
        debug = true
        stringFiles.add("strings_extra.xml")
        srcFolders.add("src/other_source")
    }

    @Before
    fun setup() {
        librarySetupTask.runCommand()
    }

    @Test
    fun `01 - (PLUGIN) locate assets files for default configuration`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(locateAssetsFiles("$temp${File.separator}$testProjectName", configuration.apply {
                assetsFiles = mutableListOf("*.json")
            }).isNotEmpty())
        }
        StringCare.resetFolder()
    }

    @Test
    fun `02 - (PLUGIN) backup assets files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            assert(backupAssetsFiles("$temp${File.separator}$testProjectName", configuration.apply {
                assetsFiles = mutableListOf("*.json")
            }).isNotEmpty())
        }
        StringCare.resetFolder()
    }

    @Test
    fun `03 - (PLUGIN) restore assets files`() {
        val temp = tempPath()
        prepareTask(temp).runCommand { _, report ->
            println(report)
            assert(
                restoreAssetsFiles("$temp${File.separator}$testProjectName", defaultMainModule).isEmpty()
            )
            assert(
                backupAssetsFiles("$temp${File.separator}$testProjectName", configuration.apply {
                    assetsFiles = mutableListOf("*.json")
                }).isNotEmpty()
            )
            assert(
                restoreAssetsFiles("$temp${File.separator}$testProjectName", defaultMainModule).isNotEmpty()
            )
        }
    }

    @Test
    fun `04 - (PLUGIN) asset obfuscation`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val key = report.extractFingerprint()
            println(key)
            assert(key.isNotEmpty())
            val files = locateAssetsFiles(
                "$temp${File.separator}$testProjectName",
                configuration.apply {
                    assetsFiles = mutableListOf("*.json")
                })
            assert(files.isNotEmpty())
            files.forEach {
                println("-------------------------------------------------------")
                val original = it.file.getContent()
                println("original: \n $original")
                obfuscateFile(
                    "$temp${File.separator}$mainModuleTest",
                    key,
                    it.file
                )
                val obfuscated = it.file.getContent()
                println("obfuscated: \n $obfuscated")
                assert(original != obfuscated)
            }
        }
    }

    @Test
    fun `05 - (PLUGIN) asset reveal`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val key = report.extractFingerprint()
            println(key)
            assert(key.isNotEmpty())
            val files = locateAssetsFiles(
                "$temp${File.separator}$testProjectName",
                configuration.apply {
                    assetsFiles = mutableListOf("*.json")
                })
            assert(files.isNotEmpty())
            files.forEach {
                println("-------------------------------------------------------")
                val original = it.file.getContent()
                println("original: \n $original")
                obfuscateFile(
                    "$temp${File.separator}$mainModuleTest",
                    key,
                    it.file
                )
                val obfuscated = it.file.getContent()
                println("obfuscated: \n $obfuscated")
                assert(original != obfuscated)
                revealFile(
                    "$temp${File.separator}$mainModuleTest",
                    key,
                    it.file
                )
                val reveal = it.file.getContent()
                println("reveal: \n $reveal")
                assert(original == reveal)
            }
        }
    }

   /*

    @Test
    fun `12 - (ANDROID COMPILATION) obfuscate xml and build (not real workflow)`() {
        val temp = tempPath()
        signingReportTask(temp).runCommand { _, report ->
            println(report)
            val files = locateFiles("$temp${File.separator}$testProjectName", configuration)
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
                configuration
            )
            filesObfuscated.forEach { file ->
                val entities = parseXML(file.file)
                assert(entities.isEmpty())
            }
        }
    }

    @Test
    fun `13 - (PLUGIN COMPILATION) plugin with no test`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
            println(report)
        }
    }

    @Test
    fun `14 - (ANDROID COMPILATION) plugin running on Android`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
        }
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            modifyForTest(temp, testProjectName)
            buildTask("$temp${File.separator}$testProjectName").runCommand { _, androidReport ->
                assert(androidReport.contains("BUILD SUCCESSFUL"))
                println(androidReport)
            }
        }
    }

    @Test
    fun `15 - (GRADLE TASK) stringcarePreview`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
        }
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            modifyForTest(temp, testProjectName)
            basicGradleTask("$temp${File.separator}$testProjectName").runCommand { _, androidReport ->
                assert(androidReport.contains("END REPORT"))
                println(androidReport)
            }

        }
    }

    @Test
    fun `16 - (GRADLE TASK) stringcareTestObfuscate`() {
        pluginBuildTask().runCommand { _, report ->
            assert(report.contains("BUILD SUCCESSFUL"))
        }
        val temp = tempPath()
        prepareTask(temp).runCommand { _, _ ->
            modifyForTest(temp, testProjectName)
            obfuscationTestGradleTask("$temp${File.separator}$testProjectName").runCommand { _, androidReport ->
                assert(androidReport.contains("END OBFUSCATION"))
                println(androidReport)
            }

        }
    }
     */

}