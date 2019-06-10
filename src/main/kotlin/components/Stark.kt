package components

import java.io.File
import java.io.FileOutputStream

class Stark {

    companion object {
        init {
            when (getOs()) {
                Os.WINDOWS -> loadLib("..\\$winLib")
                Os.OSX -> loadLib("../$osxLib")
            }
        }

        private fun loadLib(name: String) {
            // val path = (Stark::class.java.protectionDomain.codeSource.location.toURI()).path
            val inputStream = Stark::class.java.getResourceAsStream(name)
            val buffer = ByteArray(1024)
            val temp = File.createTempFile(name, "")
            val fos = FileOutputStream(temp)

            var read = inputStream.read(buffer)
            while (read != -1) {
                fos.write(buffer, 0, read)
                read = inputStream.read(buffer)
            }
            fos.close()
            inputStream.close()

            System.load(temp.absolutePath)
        }

        @JvmStatic external fun obfuscate(mainModule: String, key: String, value: ByteArray): ByteArray

        @JvmStatic external fun reveal(mainModule: String, key: String, value: ByteArray): ByteArray
    }

}