import org.junit.Test


class SCTest {

    private val logger by logger()

    @Test
    fun `terminal`() {
        val expectedMessage = "string_test"
        val response = Executor.execute("echo $expectedMessage")
        assert(response.contains(expectedMessage))
    }


    @Test
    fun `gradlew signingReport`() {
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

}