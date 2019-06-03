import org.awaitility.kotlin.await
import java.io.BufferedReader
import java.io.InputStreamReader

fun execute(command: String, processing: (line: String) -> Unit = {}) {
    val inputStream = Runtime.getRuntime().exec(command).inputStream
    val isr = InputStreamReader(inputStream)
    val buff = BufferedReader(isr)
    while (true) {
        val line = buff.readLine() ?: break
        processing(line)
    }

}