import org.junit.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SCTest {

    private val logger by logger()

    @Test
    fun firstMethodTest() {
        execute("git clone https://github.com/StringCare/KotlinSample.git") { line ->
            System.out.println(line)
        }
        execute("cd KotlinSample && ./gradlew buildDebug") { line ->
            System.out.println(line)
        }
        assert(true)
    }

    @Test
    fun secondMethodTest() {
        assert(true)
    }

}

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass) }
}