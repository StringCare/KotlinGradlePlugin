import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.concurrent.Executors
import java.util.function.Consumer

fun commandTest(command: String): Process {
    val process = Runtime.getRuntime().exec(command)
    val streamGobbler = StreamGobbler(process.inputStream, Consumer { println(it) })
    Executors.newSingleThreadExecutor().submit(streamGobbler)
    return process
}

fun Process.stringOutput(processing: (line: String) -> Unit = {}) {
    val _is = this.inputStream
    val isr = InputStreamReader(_is)
    val bufferReader = BufferedReader(isr)
    var line = bufferReader.readLine()
    while (line != null) {
        line = bufferReader.readLine()
        processing(line)
    }
}