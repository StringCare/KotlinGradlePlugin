import org.junit.Test


class SCTest {

    private val logger by logger()

    private val gradle = "/usr/local/Cellar/gradle/4.10.2/libexec/bin/gradle"

    @Test
    fun `terminal`() {
        val expectedMessage = "string_test"
        val response = Executor.execute("echo $expectedMessage")
        assert(response.contains(expectedMessage))
    }


    @Test
    fun `gradle build`() {
        val signingReport = Executor.execute(
            """
            rm -rf KotlinSample &&
            git clone https://github.com/StringCare/KotlinSample.git &&
            cd KotlinSample &&
            echo "sdk.dir=/Users/efrainespada/Library/Android/sdk" > local.properties &&
            ./gradlew build &&
            ./gradlew signingReport
        """.trimIndent()
        )
        assert(signingReport.contains("SHA1") && signingReport.contains("BUILD SUCCESSFUL"))
    }

    @Test
    fun `1 gradle build`() {
        //val build = Executor.execute("./gradlew")
        //logger.debug(build)
    }

    @Test
    fun `get fingerprint`() {
        /*
        Executor.execute("rm -rf KotlinSample")
        Executor.execute("git clone https://github.com/StringCare/KotlinSample.git")
        val moveAndShow = Executor.execute("cd KotlinSample && ./gradlew")
        logger.debug(moveAndShow)
        val resC = Executor.execute("./gradlew clean")
        logger.debug(resC)
         */
    }

    @Test
    fun firstMethodTest() {
        // verify(shellHandler.shell)?.exec("rm -rf KotlinSample")


        assert(true)
/*


        val processA = commandTest("rm -rf KotlinSample")
        processA.stringOutput {
            logger.debug(it)
        }
        val exitCodeA = processA.waitFor()
        assert(exitCodeA == 0)
        val processB = commandTest("git clone https://github.com/StringCare/KotlinSample.git")
        val exitCodeB = processB.waitFor()
        assert(exitCodeB == 0)
         */
    }

    @Test
    fun secondMethodTest() {
        assert(true)
    }

}