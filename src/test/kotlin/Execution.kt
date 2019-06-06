import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <R : Any> R.logger(): Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(this.javaClass) }
}

fun Process.outputString(): String = this.inputStream.bufferedReader().use { it.readText() }
