import java.io.*
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.system.exitProcess

class Lox {
    companion object {

        private val interpreter = Interpreter()

        private var hadError = false
        private var errorMessage = ""
        private var hadRuntimeError = false
        private var runtimeErrorMessage = ""

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
            if (hadRuntimeError) exitProcess(70)
        }

        fun runFileWithOutput(code: String): String {
            // Create a buffer and a new PrintStream to wrap it
            val buffer = ByteArrayOutputStream()
            val printStream = PrintStream(buffer)
            // Save the original System.out
            val originalOut = System.out
            val output: String

            try {
                // Redirect System.out to our printStream
                System.setOut(printStream)

                val tokens = Scanner(code).scanTokens()

                val statements = Parser(tokens).parse()
                if (hadError) {
                    return errorMessage
                }

                Resolver(interpreter).resolve(statements)
                if (hadError) {
                    return errorMessage
                }

                interpreter.interpret(statements)
                if (hadRuntimeError) {
                    return runtimeErrorMessage
                }
                if (hadError) {
                    return errorMessage
                }

                printStream.flush()
                output = buffer.toString()
            } finally {
                // Restore the original System.out
                System.setOut(originalOut)
                hadError = false
                errorMessage = ""
                hadRuntimeError = false
                runtimeErrorMessage = ""
            }

            return output
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

            println("--- TOKENS ---")
            println(tokens.joinToString("\n"))

            println("Parsing...")
            val parser = Parser(tokens)
            val statements = parser.parse()

            // stop if there was a syntax error
            if (hadError) {
                println("Parse errors encountered. Terminating.")
                return
            }

            val resolver = Resolver(interpreter)
            resolver.resolve(statements)

            if (hadError) {
                println("Resolution errors encountered. Terminating.")
                return
            }

            println("--- SYNTAX TREE ---")
            println(AstPrinter().printAst(statements))

            println("--- PROGRAM OUTPUT ---")
            interpreter.interpret(statements)
        }

        fun error(line: Int, message: String) {
            errorMessage = message
            report(line, "", message)
        }

        fun runtimeError(error: RuntimeError) {
            System.err.println("${error.message}\n[line ${error.token.line}]")
            runtimeErrorMessage = error.message
            hadRuntimeError = true
        }

        private fun report(line: Int, where: String, message: String) {
            System.err.println("[line $line] Error $where: $message")
            hadError = true
            errorMessage = "Error $where: $message"
        }

        fun error(token: Token, message: String) {
            if (token.type == TokenType.EOF) {
                report(token.line, "at end", message)
            } else {
                report(token.line, "at '${token.lexeme}'", message)
            }
        }
    }
}