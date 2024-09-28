import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Lox {
    companion object {

        var hadError = false
        fun main(args: Array<String>) {
            if (args.size > 1) {
                println("Usage: jlox [script]")
                exitProcess(64)
            } else if (args.size == 1) {
                runFile(args[0])
            } else {
                runPrompt()
            }
        }

        @Throws(IOException::class)
        private fun runFile(path: String) {
            val bytes = Files.readAllBytes(Paths.get(path))
            runLox(String(bytes, Charset.defaultCharset()))
            if (hadError) exitProcess(65)
        }

        @Throws(IOException::class)
        private fun runPrompt() {
            val input = InputStreamReader(System.`in`)
            val reader = BufferedReader(input)

            while (true) {
                print("> ")
                val line = reader.readLine() ?: break
                runLox(line)
                hadError = false
            }
        }

        private fun runLox(source: String) {
            val scanner = Scanner(source)
            val tokens = scanner.scanTokens()

            // for now, just print the tokens
            println(tokens.joinToString("\n"))
        }

        fun error(line: Int, message: String) {
            report(line, "", message)
        }

        private fun report(line: Int, where: String, message: String) {
            System.err.println("[line $line] Error $where: $message")
            hadError = true
        }
    }
}